package com.cloud_guest.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.BgiToolsApplication;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.ApplicationInfo;
import com.cloud_guest.exception.exceptions.GlobalException;
import com.cloud_guest.redis.service.RedisService;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.object.ObjectUtils;
import com.cloud_guest.wrappers.lock.LockWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.*;
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
    private static long reportedOnlineTimeout = 1 * 60 * 1000;// 1分钟

    public static long getReportedOnlineTimeout() {
        return reportedOnlineTimeout;
    }

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
     * 保存上报的在线信息
     *
     * @param key             缓存键
     * @param applicationInfo 应用信息对象
     * @return 保存结果，成功返回true
     */
    private static boolean saveReportedOnline(String key, ApplicationInfo applicationInfo,Boolean add) {
        if (ObjectUtils.isEmpty(add)) {
            add = false;
        }
        // 获取CacheService实例
        CacheService bean = SpringUtil.getBean(CacheService.class);

        // 定义锁键并获取锁对象
        String lockKey = key;
        LockWrapper lock = LockUtil.getLock(lockKey);
        // 尝试获取锁
        boolean tryLock = lock.tryLock();
        // 获取锁失败，抛出异常
        if (!tryLock) {
            throw new GlobalException("存在其他操作，请稍后再试!");
        }
        // 创建LinkedHashSet集合用于存储应用信息

        // 根据键从缓存中获取值
        Set<ApplicationInfo> hashSet = new LinkedHashSet<>();
        String values = bean.findValueByKey(key);
        if (StrUtil.isNotBlank(values)) {
            if (JSONUtil.isTypeJSONArray(values)) {
                // 是数组
                JSONUtil.toList(values, ApplicationInfo.class).forEach(hashSet::add);
            } else {
                // 不是数组
                hashSet.add(JSONUtil.toBean(values, ApplicationInfo.class));
            }
        }

        String applicationId = applicationInfo.getApplicationId();
        Long datacenterId = applicationInfo.getDatacenterId();
        hashSet.removeIf(a -> ObjectUtils.equals(a.getApplicationId(), applicationId)
                && ObjectUtils.equals(a.getDatacenterId(), datacenterId));
        if (add) {
            hashSet.add(applicationInfo);
        }

        try {
            String jsonStr = JSONUtil.toJsonStr(hashSet.stream().collect(Collectors.toList()));
            if (ModeUtil.isLocal()) {
                LocalCacheUtils.put(key, jsonStr);
            } else if (ModeUtil.isRedis()) {
                String keyRedis = KeyConstants.redis_file_json_key + key;
                RedisService redisService = SpringUtil.getBean(RedisService.class);
                redisService.save(keyRedis, jsonStr);
            }
        } finally {
            if (tryLock) {
                lock.unlock();
            }
        }
        return true;
    }

    /**
     * 上报应用在线信息
     *
     * @param applicationInfo
     */
    public static void reportedOnline(ApplicationInfo applicationInfo) {
        String onlineApplicationKey = KeyConstants.online_application_key;
        saveReportedOnline(onlineApplicationKey, applicationInfo,true);
    }
    /**
     * 移除上报
     * @param applicationInfo
     */
    public static void clearReportedOnline(ApplicationInfo applicationInfo) {
        saveReportedOnline(KeyConstants.online_application_key, applicationInfo,false);
    }

    /**
     * 检查在线并获取在线
     *
     * @param reportedOnlineTimeout 上线报备超时时间，单位为毫秒
     * @return
     */
    public static List<ApplicationInfo> checkAndGetOnline(Long reportedOnlineTimeout) {
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
            String valueByKey = bean.findValueByKey(KeyConstants.online_application_key);
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
            String applicationId = ApplicationUtil.getApplicationId();
            Long datacenterId = ApplicationUtil.getDatacenterId();
            // 将JSON字符串转换为ApplicationInfo对象列表，并过滤掉当前应用实例
            List<ApplicationInfo> checkOnlineList = checkOnlineKeys.stream().map(jsonStr -> JSONUtil.toBean(jsonStr, ApplicationInfo.class))
                    .collect(Collectors.toList());
            List<Long> datacenterIds = ApplicationUtil.getAllDatacenterIds().stream().filter(obj -> !ObjectUtils.equals(obj, datacenterId)).collect(Collectors.toList());
            List<String> applicationIds = ApplicationUtil.getAllApplicationIds().stream().filter(obj -> !ObjectUtils.equals(obj, applicationId)).collect(Collectors.toList());
            //datacenterIds 和 applicationIds 1对1
            // 遍历并清理每个异常重启键
            long currentTimeMillis = System.currentTimeMillis();
            List<ApplicationInfo> outList = new ArrayList<>();

            // 使用 Set 提高查找效率 O(1) vs O(N)
            Set<String> normalApplicationIds = new HashSet<>();
            Set<Long> normalDatacenterIds = new HashSet<>();
            List<ApplicationInfo> onlineList = new ArrayList<>();

            for (ApplicationInfo applicationInfo : checkOnlineList) {
                Long timeStamp = applicationInfo.getTimeStamp();
                if (currentTimeMillis - timeStamp > reportedOnlineTimeout) {
                    //超出上线报备时间间隔未报备上线
                    //标记需要处理
                    outList.add(applicationInfo);
                } else {
                    // 正常在线，保留
                    normalApplicationIds.add(applicationInfo.getApplicationId());
                    normalDatacenterIds.add(applicationInfo.getDatacenterId());
                    onlineList.add(applicationInfo);
                }
            }

            // 步骤 2: 找出孤立的 ID（没有对应实例的 ID）
            List<String> orphanApplicationIds = applicationIds.stream()
                    .filter(id -> !normalApplicationIds.contains(id))
                    .collect(Collectors.toList());

            List<Long> orphanDatacenterIds = datacenterIds.stream()
                    .filter(id -> !normalDatacenterIds.contains(id))
                    .collect(Collectors.toList());


            //// 步骤 3: 验证数据一致性
            //if (orphanApplicationIds.size() != orphanDatacenterIds.size()) {
            //    log.error("数据不一致！缓冲离线的应用 ID 数量：{}, 缓冲离线的数据中心 ID 数量：{}",
            //            orphanApplicationIds.size(), orphanDatacenterIds.size());
            //    log.error("这可能意味着存在未配对的 ID，需要人工介入检查");
            //    // 不抛异常，继续处理能处理的部分
            //}

            // 步骤 4: 缓冲离线的 ID 配对添加到待清理列表
            int maxSize = Math.max(orphanApplicationIds.size(), orphanDatacenterIds.size());
            for (int i = 0; i < maxSize; i++) {
                String applicationIdTemp = null;
                Long datacenterIdTemp = null;
                try {
                    applicationIdTemp = orphanApplicationIds.get(i);
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
                try {
                    datacenterIdTemp = orphanDatacenterIds.get(i);
                } catch (Exception e) {
                    log.warn(e.getMessage());
                }
                ApplicationInfo orphanApp = new ApplicationInfo(
                        applicationIdTemp,
                        datacenterIdTemp,
                        null
                );
                log.debug("发现缓冲离线应用实例，加入清理队列：{}", orphanApp);
                outList.add(orphanApp);
            }
            log.debug("在线检查完成 - 正常在线：{}, 超时离线：{}",
                    normalApplicationIds.size(), outList.size());

            for (ApplicationInfo applicationInfo : outList) {
                applicationInfo.setTimeStamp(null);
                bean.saveId(outlineApplicationKey, JSONUtil.toJsonStr(applicationInfo));
            }

            return onlineList;
        } finally {
            if (tryLock) {
                lock.unlock();
            }
        }

    }

    /**
     * 清除离线键
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
            if (CollUtil.isNotEmpty(outlineList)) {
                log.warn("发现 {} 个离线的应用实例需要清理", outlineList.size());
                for (ApplicationInfo applicationInfo : outlineList) {
                    ApplicationUtil.destroyApplicationIdAndDatacenterId(applicationInfo.getApplicationId(), applicationInfo.getDatacenterId());
                }
                bean.removeByKey(outlineApplicationKey);
            }

        } finally {
            if (tryLock) {
                lock.unlock();
            }
        }
    }


    /**
     * 清理重启键
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
                    log.warn("发现 {} 个重启下线的应用实例需要清理", staleKeys.size());
                    // 遍历并清理每个异常重启键
                    for (ApplicationInfo key : staleKeys) {
                        try {
                            // 分割键获取应用ID和数据中心ID
                            String applicationId = key.getApplicationId();
                            Long datacenterId = key.getDatacenterId();
                            // 记录清理信息
                            log.debug("清理重启应用实例 - applicationId:{}, datacenterId:{}", applicationId, datacenterId);
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