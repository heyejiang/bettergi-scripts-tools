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
    public static List<TaskInfo> TASKS = new ArrayList<>();

    public void initTasks() {
        List<TaskInfo> tasks = new ArrayList<>();
        tasks.add(new TaskDef(QuartzName.SECONDS_1, QuartzGroup.DEFAULT, Seconds1Job.class, CronTemplate.SECONDS).buildToTaskInfo());
        tasks.add(new TaskDef(QuartzName.MINUTE_1, QuartzGroup.DEFAULT, Minute1Job.class, CronTemplate.MINUTE).buildToTaskInfo());
        tasks.add(new TaskDef(QuartzName.CLOCK_0, QuartzGroup.DEFAULT, Clock0Job.class, CronTemplate.CLOCK).buildToTaskInfo());
        TASKS.addAll(tasks);
    }

    @PostConstruct
    public void init() throws SchedulerException {
        initTasks();
        Scheduler scheduler = SpringUtil.getBean(Scheduler.class);
        log.info("{}开始动态注册 Quartz 定时任务... 共 {} 个", TASK_LOG_KEY, TASKS.size());

        for (TaskInfo taskInfo : TASKS) {
            try {
                registerTask(taskInfo);
                log.info("{}任务注册成功：{} → {}", TASK_LOG_KEY, taskInfo.getName(), taskInfo.getCronExpression());
            } catch (Exception e) {
                log.error("{}任务注册失败：{}，原因：{}", TASK_LOG_KEY, taskInfo.getName(), e.getMessage(), e);
            }
        }

        // 可选：启动时检查 scheduler 是否已经启动
        if (!scheduler.isStarted()) {
            scheduler.start();
        }

        log.info("所有 Quartz 任务动态注册完成");
    }

    /**
     * 注册一个定时任务
     *
     * @param def 任务定义对象，包含任务的基本信息
     * @throws SchedulerException 如果调度器出现异常
     */
    public static void registerTask(TaskDef def) throws SchedulerException {
        TaskInfo taskInfo = def.buildToTaskInfo();
        registerTask(taskInfo);
    }

    /**
     * 注册一个定时任务
     *
     * @param info 任务定义对象，包含任务的基本信息
     * @throws SchedulerException 如果调度器出现异常
     */
    public static void registerTask(TaskInfo info) throws SchedulerException {
        String name = info.getName();
        String group = info.getGroup();
        Class<? extends Job> jobClass = info.getJobClass();
        String cronExpression = info.getCronExpression();
        // 创建JobKey，用于唯一标识一个任务
        JobKey jobKey = new JobKey(name, group);
        // 创建TriggerKey，用于唯一标识一个触发器
        TriggerKey triggerKey = new TriggerKey(name, group);
        // 调用重载的registerTask方法完成注册
        registerTask(jobKey, triggerKey, jobClass, cronExpression);
    }

    /**
     * 注册定时任务
     *
     * @param jobKey     任务标识，用于唯一标识一个任务
     * @param triggerKey 触发器标识，用于唯一标识一个触发器
     * @param jobClass   任务类，需要执行的具体业务逻辑
     * @param cronExpr   cron表达式，定义任务的执行时间规则
     * @throws SchedulerException 调度器异常
     */
    public static void registerTask(JobKey jobKey, TriggerKey triggerKey, Class<? extends Job> jobClass, String cronExpr) throws SchedulerException {
        // 创建JobDetail，定义任务的基本信息
        JobDetail jobDetail = JobBuilder.newJob(jobClass)
                .withIdentity(jobKey)    // 设置任务标识
                .storeDurably()           // 即使没有 trigger 也保留
                //.requestRecovery()      // 需要的话开启
                //.setJobData(new JobDataMap(...)) // 可以设置任务相关的数据
                .build();

        // 2. Trigger
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

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TaskInfo {
        String name;
        String group;
        Class<? extends Job> jobClass;
        String cronExpression;
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

        public TaskInfo buildToTaskInfo() {
            return new TaskInfo(name.name(), group.name(), jobClass, cron());
        }
    }

    // ------------------ 可选扩展功能 ------------------

    // 未来如果需要动态添加任务（比如管理后台调用）
    public static void addTaskManually(String name, String group,
                                       Class<? extends Job> jobClass,
                                       String cronExpr) throws SchedulerException {
        JobKey jobKey = new JobKey(name, group);
        // 创建TriggerKey，用于唯一标识一个触发器
        TriggerKey triggerKey = new TriggerKey(name, group);
        registerTask(jobKey, triggerKey, jobClass, cronExpr);
    }

    // 暂停某个任务
    public static void pauseTask(String name, String group) throws SchedulerException {
        Scheduler scheduler = SpringUtil.getBean(Scheduler.class);
        TriggerKey tk = new TriggerKey(name, group);
        scheduler.pauseTrigger(tk);
    }

    // 恢复
    public static void resumeTask(String name, String group) throws SchedulerException {
        Scheduler scheduler = SpringUtil.getBean(Scheduler.class);
        TriggerKey tk = new TriggerKey(name, group);
        scheduler.resumeTrigger(tk);
    }
}
