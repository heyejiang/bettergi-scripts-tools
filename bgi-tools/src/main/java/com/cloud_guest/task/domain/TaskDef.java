package com.cloud_guest.task.domain;

import cn.hutool.core.util.StrUtil;
import com.cloud_guest.task.enums.CronTemplate;
import com.cloud_guest.task.enums.QuartzGroup;
import com.cloud_guest.task.enums.QuartzName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.quartz.Job;

/**
 * @Author yan
 * @Date 2026/3/2 22:11:03
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDef {
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
