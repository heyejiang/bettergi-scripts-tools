package com.cloud_guest.task.job;


import cn.hutool.cron.CronUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.domain.ApplicationInfo;
import com.cloud_guest.task.config.QuartzConfig;
import com.cloud_guest.task.domain.TaskInfo;
import com.cloud_guest.task.dstributed.DistributedJob;
import com.cloud_guest.task.util.QuartzUtil;
import com.cloud_guest.utils.ApplicationContextHolder;
import com.cloud_guest.utils.ApplicationUtil;
import com.cloud_guest.utils.DateUtils;
import com.cloud_guest.utils.object.ObjectUtils;
import com.cloud_guest.utils.task.CronUtils;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class Minute1Job extends DistributedJob {

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {


    }

}
