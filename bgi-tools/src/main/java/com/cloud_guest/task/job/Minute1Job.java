package com.cloud_guest.task.job;


import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.domain.ApplicationInfo;
import com.cloud_guest.task.dstributed.DistributedJob;
import com.cloud_guest.utils.ApplicationContextHolder;
import com.cloud_guest.utils.ApplicationUtil;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class Minute1Job extends DistributedJob {

    //@Override
    //public void executeDistributed(JobExecutionContext var1) throws JobExecutionException {
    //    log.info("~~~~~~~~~~~~~~~~~~~~~~~~~Minute1Job~~~~~~~~~~~~~~~~~~~~~~~~~");
    //}

    //@Override
    //public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    //    log.info("~~~~~~~~~~~~~~~~~~~~~~~~~Minute1Job~~~~~~~~~~~~~~~~~~~~~~~~~");
    //}
    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        ThreadPoolTaskExecutor executor = SpringUtil.getBean(ThreadPoolTaskExecutor.class);
        //Minute1Job.super.executeInternal(context);
        //log.debug("~~~~~~~~~~~~~~~~~~~~~~~~~{}~~~~~~~~~~~~~~~~~~~~~~~~~",this.getClass().getSimpleName());
        ////CustThreadPoolTaskExecutor runAsync = new CustThreadPoolTaskExecutor().setCustThreadNamePrefix("runAsync");
        CompletableFuture.runAsync(()->{
            //上报在线
            ApplicationInfo applicationInfo = ApplicationUtil.getApplicationInfo();
            if (applicationInfo != null){
                //避免污染缓存数据
                ApplicationInfo reportedOnline = applicationInfo.toReportedOnline();
                ApplicationContextHolder.reportedOnline(reportedOnline);
            }
        }, executor);
    }

}
