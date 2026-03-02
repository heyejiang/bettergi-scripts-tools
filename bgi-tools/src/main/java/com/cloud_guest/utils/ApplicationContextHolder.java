package com.cloud_guest.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.BgiToolsApplication;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.ApplicationInfo;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.object.ObjectUtils;
import com.cloud_guest.wrappers.lock.LockWrapper;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
                LockWrapper lock = LockUtil.getLock(restartKey);
                boolean tryLock = lock.tryLock(1l, TimeUnit.MINUTES);
                if (!tryLock) {
                    throw new GlobalException("获取锁失败:" + restartKey);
                }
                try {
                    //String id = applicationId + "=" + datacenterId;
                    ApplicationInfo applicationInfo = new ApplicationInfo(applicationId, datacenterId, Long.valueOf(System.currentTimeMillis()));
                    //设置重启key
                    SpringUtil.getBean(CacheService.class).saveId(restartKey, JSONUtil.toJsonStr(applicationInfo));
                } finally {
                    if (tryLock) {
                        lock.unlock();
                    }
                }

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

    /**
     * 上报应用在线信息
     *
     * @param applicationId 应用ID
     * @param datacenterId  数据中心ID
     */
    public static void reportedOnline(String applicationId, Long datacenterId) {
        // 创建应用信息对象，包含应用ID、数据中心ID和当前时间戳
        ApplicationInfo applicationInfo = new ApplicationInfo(applicationId, datacenterId, Long.valueOf(System.currentTimeMillis()));
        // 通过Spring工具类获取CacheService的Bean，将应用信息以JSON字符串形式保存到缓存中
        // 使用预定义的键名常量online_application_key作为缓存键
        reportedOnline(applicationInfo);
    }

    /**
     * 上报应用在线信息
     * @param applicationInfo
     */
    public static void reportedOnline(ApplicationInfo applicationInfo) {
        String onlineApplicationKey = KeyConstants.online_application_key;
        LockWrapper lock = LockUtil.getLock(onlineApplicationKey);
        boolean tryLock = lock.tryLock(1l, TimeUnit.MINUTES);
        if (!tryLock) {
            throw new GlobalException("获取锁失败:" + onlineApplicationKey);
        }
        try {
            applicationInfo.setTimeStamp(System.currentTimeMillis());
            SpringUtil.getBean(CacheService.class).saveId(onlineApplicationKey, JSONUtil.toJsonStr(applicationInfo));
        } finally {
            if (tryLock) {
                lock.unlock();
            }
        }
    }

    /**
     * 检查在线状态的静态方法
     *
     * @param reportedOnlineTimeout 上线报备超时时间，单位为毫秒
     */
    public static void checkOnline(Long reportedOnlineTimeout) {
        // 如果传入的超时时间为空，则设置默认超时时间为5分钟
        if (reportedOnlineTimeout == null) {
            reportedOnlineTimeout = 1000 * 60 * 5l;
        }
        // 从Spring容器中获取CacheService的Bean实例
        CacheService bean = SpringUtil.getBean(CacheService.class);
        // 定义用于存储上线应用信息的缓存键
        String outlineApplicationKey = KeyConstants.outline_application_key;

        // 获取指定键的锁对象
        LockWrapper lock = LockUtil.getLock(outlineApplicationKey);
        // 尝试获取锁，最多等待1分钟
        boolean tryLock = lock.tryLock(1l, TimeUnit.MINUTES);
        // 如果获取锁失败，抛出全局异常
        if (!tryLock) {
            throw new GlobalException("获取锁失败:" + outlineApplicationKey);
        }

        try {
            // 从缓存中获取键对应的值
            String valueByKey = bean.findValueByKey(outlineApplicationKey);
            // 创建用于存储检查在线键的列表
            List<String> checkOnlineKeys = new ArrayList<>();  // 修复：使用可变列表

            // 如果获取到的值不为空
            if (StrUtil.isNotBlank(valueByKey)) {
                // 判断值是否为JSONArray类型
                if (JSONUtil.isTypeJSONArray(valueByKey)) {
                    // 如果是JSONArray类型，将所有元素添加到列表中
                    checkOnlineKeys.addAll(JSONUtil.toList(valueByKey, String.class));
                } else {
                    // 如果不是JSONArray类型，直接将值添加到列表中
                    checkOnlineKeys.add(valueByKey);
                }
            }

            // 将JSON字符串转换为ApplicationInfo对象列表，并过滤掉当前应用实例
            List<ApplicationInfo> checkOnlineList = checkOnlineKeys.stream().map(jsonStr -> JSONUtil.toBean(jsonStr, ApplicationInfo.class))
                    // 过滤掉当前应用实例 绝对在线
                    .filter(obj -> !ObjectUtils.equals(obj.getApplicationId(), ApplicationUtil.getApplicationId()))
                    .collect(Collectors.toList());
            // 遍历并清理每个异常重启键
            long currentTimeMillis = System.currentTimeMillis();
            List<ApplicationInfo> outList = new ArrayList<>();
            for (ApplicationInfo applicationInfo : checkOnlineList) {
                Long timeStamp = applicationInfo.getTimeStamp();
                if (currentTimeMillis - timeStamp > reportedOnlineTimeout) {
                    //超出上线报备时间间隔未报备上线
                    //标记需要处理
                    outList.add(applicationInfo);
                }
            }

            for (ApplicationInfo applicationInfo : outList) {
                bean.saveId(outlineApplicationKey, JSONUtil.toJsonStr(applicationInfo));
            }
        } finally {
            if (tryLock) {
                lock.unlock();
            }
        }

    }

    /**
     * 清除轮廓键的方法
     * 该方法用于清除所有与轮廓相关的键值，可能是为了重置状态或释放资源
     */
    public static void clearOutlineKeys() {
        CacheService bean = SpringUtil.getBean(CacheService.class);
        String outlineApplicationKey = KeyConstants.outline_application_key;

        LockWrapper lock = LockUtil.getLock(outlineApplicationKey);
        boolean tryLock = lock.tryLock(1l, TimeUnit.MINUTES);
        if (!tryLock) {
            throw new GlobalException("获取锁失败:" + outlineApplicationKey);
        }
        try {
            String valueByKey = bean.findValueByKey(outlineApplicationKey);
            // 创建一个可变列表用于存储重启键
            List<String> keys = new ArrayList<>();  // 修复：使用可变列表

            // 如果获取到的值不为空
            if (StrUtil.isNotBlank(valueByKey)) {
                // 判断值是否为JSONArray类型
                if (JSONUtil.isTypeJSONArray(valueByKey)) {
                    // 如果是JSONArray类型，将所有元素添加到列表中
                    keys.addAll(JSONUtil.toList(valueByKey, String.class));
                } else {
                    // 如果不是JSONArray类型，直接将值添加到列表中
                    keys.add(valueByKey);
                }
            }
            List<ApplicationInfo> outlineList = keys.stream().map(jsonStr -> JSONUtil.toBean(jsonStr, ApplicationInfo.class)).collect(Collectors.toList());

            for (ApplicationInfo applicationInfo : outlineList) {
                ApplicationUtil.destroyApplicationIdAndDatacenterId(applicationInfo.getApplicationId(), applicationInfo.getDatacenterId());
            }
            bean.removeByKey(outlineApplicationKey);
        } finally {
            if (tryLock) {
                lock.unlock();
            }
        }
    }


    /**
     * 清理系统中异常下线的应用实例的重启键
     * 该方法会检查并清理那些已经不在当前应用实例列表中的重启键
     */
    public static void clearRestartKeys() {
        try {
            // 从Spring容器中获取CacheService的Bean实例
            CacheService bean = SpringUtil.getBean(CacheService.class);
            LockWrapper lock = LockUtil.getLock(restartKey);
            boolean tryLock = lock.tryLock(1l, TimeUnit.MINUTES);
            if (!tryLock) {
                throw new GlobalException("获取锁失败:" + restartKey);
            }
            try {
                // 根据重启键的键名获取存储的值
                String valueByKey = bean.findValueByKey(restartKey);
                // 创建一个可变列表用于存储重启键
                List<String> restartKeys = new ArrayList<>();  // 修复：使用可变列表

                // 如果获取到的值不为空
                if (StrUtil.isNotBlank(valueByKey)) {
                    // 判断值是否为JSONArray类型
                    if (JSONUtil.isTypeJSONArray(valueByKey)) {
                        // 如果是JSONArray类型，将所有元素添加到列表中
                        restartKeys.addAll(JSONUtil.toList(valueByKey, String.class));
                    } else {
                        // 如果不是JSONArray类型，直接将值添加到列表中
                        restartKeys.add(valueByKey);
                    }
                }

                // 获取所有当前运行的应用ID列表
                List<String> applicationIds = ApplicationUtil.getAllApplicationIds();
                // 筛选出需要清理的异常重启键
                List<ApplicationInfo> staleKeys = restartKeys.stream()
                        .map(jsonStr -> JSONUtil.toBean(jsonStr, ApplicationInfo.class))
                        //重启后会重新生成
                        .filter(key -> !applicationIds.contains(key.getApplicationId()))
                        .collect(Collectors.toList());

                // 如果存在需要清理的异常重启键
                if (CollUtil.isNotEmpty(staleKeys)) {
                    // 记录需要清理的异常重启键数量
                    log.warn("发现 {} 个异常下线的应用实例需要清理", staleKeys.size());
                    // 遍历并清理每个异常重启键
                    for (ApplicationInfo key : staleKeys) {
                        try {
                            // 分割键获取应用ID和数据中心ID
                            String applicationId = key.getApplicationId();
                            Long datacenterId = key.getDatacenterId();
                            // 记录清理信息
                            log.debug("清理异常应用实例 - applicationId:{}, datacenterId:{}", applicationId, datacenterId);
                            // 清理应用实例
                            ApplicationUtil.destroyApplicationIdAndDatacenterId(applicationId, datacenterId);
                        } catch (Exception e) {
                            log.error("清理重启键失败: {}", key, e);
                        }
                    }
                    // 清理重启键
                    bean.removeByKey(restartKey);
                }
            } finally {
                if (tryLock) {
                    lock.unlock();
                }
            }
        } catch (Exception e) {
            log.error("清理重启键过程出现异常", e);
        }
    }

}