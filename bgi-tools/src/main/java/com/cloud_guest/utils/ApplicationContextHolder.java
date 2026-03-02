package com.cloud_guest.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.BgiToolsApplication;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/27 21:49:23
 * @Description
 */
// 1. 先创建一个工具类保存上下文和启动参数
@Component
@Slf4j
public class ApplicationContextHolder {

    private static ConfigurableApplicationContext context;
    private static String[] args;
    private static String restartKey = KeyConstants.all_application_key + ":" + KeyConstants.restart_key;

    public static void setContext(ConfigurableApplicationContext ctx, String[] startupArgs) {
        context = ctx;
        args = startupArgs;
    }

    public static void restart() {
        String applicationId = ApplicationUtil.getApplicationId();
        Long datacenterId = ApplicationUtil.getDatacenterId();
        Thread restartThread = new Thread(() -> {
            try {
                String id = applicationId + "=" + datacenterId;
                //设置重启key
                SpringUtil.getBean(CacheService.class).saveId(restartKey, id);
                String active = SpringUtil.getBean(Environment.class).getProperty("spring.profiles.active", String.class);
                Thread.sleep(1000);  // 可选：缓冲时间

                // 1. 关闭旧上下文（触发优雅关闭）
                if (context != null && context.isRunning()) {
                    context.close();
                }

                // 2. 显式创建 SpringApplication 并强制设为 SERVLET 类型
                SpringApplication application = new SpringApplication(BgiToolsApplication.class);  // YourApplication 换成你的 @SpringBootApplication 类
                application.setWebApplicationType(WebApplicationType.SERVLET);  // ← 关键这一行！

                // 可选：如果有其他配置（如 profiles、banner 等），在这里设置
                application.setAdditionalProfiles(active);
                // application.setBannerMode(Banner.Mode.OFF);

                // 3. 重新 run
                ConfigurableApplicationContext newContext = application.run(args);

                // 更新 holder
                setContext(newContext, args);

                log.info("应用已成功重启");

            } catch (Exception e) {
                log.error("重启失败: " + e.getMessage());
                e.printStackTrace();
                // 可选：System.exit(1); 或其他处理
            }
        });

        restartThread.setDaemon(false);
        restartThread.start();
    }

public static void clearRestartKeys() {
    try {
        CacheService bean = SpringUtil.getBean(CacheService.class);
        String valueByKey = bean.findValueByKey(restartKey);
        List<String> restartKeys = new ArrayList<>();  // 修复：使用可变列表

        if (StrUtil.isNotBlank(valueByKey)) {
            if (JSONUtil.isTypeJSONArray(valueByKey)) {
                restartKeys.addAll(JSONUtil.toList(valueByKey, String.class));
            } else {
                restartKeys.add(valueByKey);
            }
        }

        List<String> applicationIds = ApplicationUtil.getAllApplicationIds();
        List<String> staleKeys = restartKeys.stream()
            .filter(key -> {
                try {
                    // 验证键格式
                    String[] parts = key.split("=", 2);
                    return parts.length == 2 && !applicationIds.contains(key);
                } catch (Exception e) {
                    log.warn("无效的重启键格式: {}", key);
                    return true; // 无效格式也清理掉
                }
            })
            .collect(Collectors.toList());

        if (CollUtil.isNotEmpty(staleKeys)) {
            log.warn("发现 {} 个异常下线的应用实例需要清理", staleKeys.size());
            for (String key : staleKeys) {
                try {
                    String[] parts = key.split("=", 2);
                    String applicationId = parts[0];
                    Long datacenterId = Long.parseLong(parts[1]);
                    log.debug("清理异常应用实例 - applicationId:{}, datacenterId:{}", applicationId, datacenterId);
                    ApplicationUtil.destroyApplicationIdAndDatacenterId(applicationId, datacenterId);
                } catch (Exception e) {
                    log.error("清理重启键失败: {}", key, e);
                }
            }
        }
    } catch (Exception e) {
        log.error("清理重启键过程出现异常", e);
    }
}

}