package com.cloud_guest.task.domain;


import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.task.enums.CronTemplate;
import com.cloud_guest.task.enums.QuartzGroup;
import com.cloud_guest.task.enums.QuartzName;
import com.cloud_guest.task.job.Clock0Job;
import com.cloud_guest.task.job.Minute1Job;
import com.cloud_guest.task.job.Seconds1Job;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Yao
 * @date 2024/5/28 17:52
 */
@Configuration
@Slf4j
public class QuartzConfig {
    public static final String TASK_LOG_KEY = "[定时任务]";

    // 如果你已经有 QuartzName 枚举，可以继续用它
    // 这里我们用一个简单的定义类，更容易阅读和扩展
    public static List<TaskDef> TASKS = new ArrayList<>();

    public void initTasks() {
        List<TaskDef> tasks = new ArrayList<>();
        tasks.add(new TaskDef(QuartzName.SECONDS_1, QuartzGroup.DEFAULT, Seconds1Job.class, CronTemplate.SECONDS));
        tasks.add(new TaskDef(QuartzName.MINUTE_1, QuartzGroup.DEFAULT, Minute1Job.class, CronTemplate.MINUTE));
        tasks.add(new TaskDef(QuartzName.CLOCK_0, QuartzGroup.DEFAULT, Clock0Job.class, CronTemplate.CLOCK));
        TASKS.addAll(tasks);
    }

    @PostConstruct
    public void init() throws SchedulerException {
        initTasks();
        Scheduler scheduler = SpringUtil.getBean(Scheduler.class);
        log.info("{}开始动态注册 Quartz 定时任务... 共 {} 个", TASK_LOG_KEY, TASKS.size());

        for (TaskDef def : TASKS) {
            try {
                registerTask(def);
                log.info("{}任务注册成功：{} → {}", TASK_LOG_KEY, def.name, def.cron());
            } catch (Exception e) {
                log.error("{}任务注册失败：{}，原因：{}", TASK_LOG_KEY, def.name, e.getMessage(), e);
            }
        }

        // 可选：启动时检查 scheduler 是否已经启动
        if (!scheduler.isStarted()) {
            scheduler.start();
        }

        log.info("所有 Quartz 任务动态注册完成");
    }

    private void registerTask(TaskDef def) throws SchedulerException {
        JobKey jobKey = new JobKey(def.name.name(), def.group.name());
        TriggerKey triggerKey = new TriggerKey(def.name.name(), def.group.name());

        // 1. JobDetail
        JobDetail jobDetail = JobBuilder.newJob(def.jobClass)
                .withIdentity(jobKey)
                .storeDurably()           // 即使没有 trigger 也保留
                //.requestRecovery()      // 需要的话开启
                //.setJobData(new JobDataMap(...))
                .build();

        // 2. Trigger
        String cronExpr = String.format(def.cronTemplate.getCronTemplate(), def.value);
        CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpr)
                // 根据业务选择 misfire 策略（常见选项）
                .withMisfireHandlingInstructionDoNothing()
                //.withMisfireHandlingInstructionFireAndProceed()
                //.withMisfireHandlingInstructionIgnoreMisfires()
                ;

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(triggerKey)
                .forJob(jobKey)
                .withSchedule(scheduleBuilder)
                //.startNow()           // 默认就是
                //.endAt(someDate)
                .build();
        Scheduler scheduler = SpringUtil.getBean(Scheduler.class);
        // 3. 注册（幂等：存在则替换）
        if (scheduler.checkExists(jobKey)) {
            log.warn("{}任务已存在，进行覆盖更新：{}", TASK_LOG_KEY, jobKey);
            scheduler.deleteJob(jobKey);
        }

        scheduler.scheduleJob(jobDetail, trigger);
    }

    // 辅助记录类（也可以用 record，如果用 Java 14+）
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TaskDef {
        QuartzName name;
        QuartzGroup group;
        Class<? extends Job> jobClass;
        CronTemplate cronTemplate;
        String value;

        String cron() {
            return String.format(cronTemplate.getCronTemplate(), value);
        }

        public TaskDef(QuartzName name, QuartzGroup group, Class<? extends Job> jobClass, CronTemplate cronTemplate) {
            String[] split = name.name().split("_", 2);
            String value = split[1];
            if (StrUtil.isBlank(value)) {
                throw new IllegalArgumentException("任务名称格式不正确，请检查 QuartzName 枚举");
            }
            try {
                long num = Long.parseLong(value);
                if (num < 0) {
                    throw new IllegalArgumentException("任务名称必须为非负整数 如SECONDS_1");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("任务名称格式不正确，请检查 QuartzName 枚举", e);
            }

            this.name = name;
            this.group = group;
            this.jobClass = jobClass;
            this.cronTemplate = cronTemplate;
            this.value = value;
        }
    }

    // ------------------ 可选扩展功能 ------------------

    // 未来如果需要动态添加任务（比如管理后台调用）
    public void addTaskManually(QuartzName name, QuartzGroup group,
                                Class<? extends Job> jobClass,
                                CronTemplate template, String value) throws SchedulerException {
        TaskDef def = new TaskDef(name, group, jobClass, template, value);
        registerTask(def);
    }

    // 暂停某个任务
    public void pauseTask(QuartzName name, QuartzGroup group) throws SchedulerException {
        Scheduler scheduler = SpringUtil.getBean(Scheduler.class);
        TriggerKey tk = new TriggerKey(name.name(), group.name());
        scheduler.pauseTrigger(tk);
    }

    // 恢复
    public void resumeTask(QuartzName name, QuartzGroup group) throws SchedulerException {
        Scheduler scheduler = SpringUtil.getBean(Scheduler.class);
        TriggerKey tk = new TriggerKey(name.name(), group.name());
        scheduler.resumeTrigger(tk);
    }
}
