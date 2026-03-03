package com.cloud_guest.task.util;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.task.domain.TaskDef;
import com.cloud_guest.task.domain.TaskInfo;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

/**
 * @Author yan
 * @Date 2026/3/2 22:14:39
 * @Description
 */
@Slf4j
public class QuartzUtil {
    public static final String TASK_LOG_KEY = "[定时任务]";

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

    @SneakyThrows
    public static Long getNextIntervalMillis(String cron)  {
        CronExpression expr = new CronExpression(cron);

        Date now = new Date();
        Date next1 = expr.getNextValidTimeAfter(now);
        if (next1 == null) return null;

        Date next2 = expr.getNextValidTimeAfter(next1);
        if (next2 == null) return null;

        long l = next2.getTime() - next1.getTime();
        return Long.valueOf(l);   // 毫秒
    }

}
