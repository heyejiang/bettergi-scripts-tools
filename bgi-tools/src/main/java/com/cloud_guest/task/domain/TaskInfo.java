package com.cloud_guest.task.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.quartz.Job;

/**
 * @Author yan
 * @Date 2026/3/2 22:10:17
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskInfo {
    String name;
    String group;
    Class<? extends Job> jobClass;
    String cronExpression;
}
