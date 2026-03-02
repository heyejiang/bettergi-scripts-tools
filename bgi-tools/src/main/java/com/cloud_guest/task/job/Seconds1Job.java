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
        long reportedOnlineTimeout = ApplicationContextHolder.getReportedOnlineTimeout();

        CompletableFuture.runAsync(()->{
            // 按顺序执行，确保数据一致性
            log.debug("检查在线");
            ApplicationContextHolder.checkOnline(reportedOnlineTimeout);
            log.debug("清理离线");
            ApplicationContextHolder.clearOutlineKeys();
            log.debug("清理重启");
            ApplicationContextHolder.clearRestartKeys();
        }, executor);
    }
}
