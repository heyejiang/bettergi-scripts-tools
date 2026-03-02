package com.cloud_guest.task.job;

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.task.dstributed.DistributedJob;
import com.cloud_guest.utils.ApplicationContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * @Author yan
 * @Date 2026/3/2 20:26:08
 * @Description
 */
@Slf4j
@Component
public class Seconds1Job  extends DistributedJob {
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        ThreadPoolTaskExecutor executor = SpringUtil.getBean(ThreadPoolTaskExecutor.class);
        //Seconds1Job.super.executeInternal(context);
        //log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~{}~~~~~~~~~~~~~~~~~~~~~~~~~",this.getClass().getSimpleName());
        ////CustThreadPoolTaskExecutor runAsync = new CustThreadPoolTaskExecutor().setCustThreadNamePrefix("runAsync");
        CompletableFuture.runAsync(()->{
            //log.debug("===> {} <===","runAsync");
            //删除重启key 防止异常下线未清理
            ApplicationContextHolder.clearRestartKeys();
        }, executor);
    }
}
