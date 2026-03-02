package com.cloud_guest.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import cn.hutool.json.JSONUtil;
import com.cloud_guest.aop.bean.AbsBean;
import com.cloud_guest.constants.KeyConstants;
import com.cloud_guest.domain.ApplicationInfo;
import com.cloud_guest.domain.Cache;
import com.cloud_guest.service.CacheService;
import com.cloud_guest.utils.object.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author yan
 * @Date 2026/2/23 20:45:43
 * @Description
 */
@Slf4j
@Component
public class ApplicationUtil implements AbsBean {
    private static ApplicationInfo applicationInfo = new ApplicationInfo(null, 0l, System.currentTimeMillis());
    //public static List<String> nodeApplicationIds = new ArrayList<>();
    private static final String application_key = KeyConstants.all_application_key;
    private static final String application_datacenter_key = KeyConstants.all_application_datacenter_key;
    private static boolean initEnd = false;
    @Resource
    private CacheService cacheService;

    @PostConstruct
    @Override
    public void init() {
        AbsBean.super.init();
        log.debug("==> 初始化ApplicationUtil <==");
        //上线
        String id = System.currentTimeMillis() + "@" + IdUtil.fastUUID();
        applicationInfo.setApplicationId(id);

        initApplicationId();
        initDatacenterId();
        initEnd = true;
    }

    public void initApplicationId() {
        List<String> applicationIds = getAllApplicationIds();
        applicationIds.add(applicationInfo.getApplicationId());
        cacheService.save(application_key, JSONUtil.toJsonStr(applicationIds));
    }

    public static ApplicationInfo getApplicationInfo() {
        if (!initEnd){
            return null;
        }
        return applicationInfo;
    }

    /**
     * 初始化数据中心ID的方法
     * 该方法主要负责从缓存中获取已有的数据中心ID集合，并生成一个新的数据中心ID
     * 新生成的ID会在已有最大ID基础上递增，然后更新回缓存中
     */
    public void initDatacenterId() {
        // 从Spring容器中获取CacheService实例的代码被注释掉了
        //CacheService cacheService = SpringUtil.getBean(CacheService.class);
        //初始化workId的变量（虽然变量名是workId，但实际处理的是datacenterId）
        String works = cacheService.findById(application_datacenter_key);
        LinkedHashSet<Long> datacenterIds = new LinkedHashSet<>();
        if (StrUtil.isNotBlank(works)) {
            if (JSONUtil.isTypeJSONArray(works)) {
                JSONUtil.toList(works, String.class).stream().map(Long::valueOf).forEach(datacenterIds::add);
            } else {
                datacenterIds.add(Long.valueOf(works));
            }
        }
        Long datacenterId = getDatacenterId();
        datacenterId++;
        if (datacenterIds.size() > 0) {
            datacenterId += datacenterIds.stream().filter(ObjectUtils::isNotEmpty).mapToLong(Long::longValue).max().getAsLong();
        }
        applicationInfo.setDatacenterId(datacenterId);
        datacenterIds.add(datacenterId);
        cacheService.save(application_datacenter_key, JSONUtil.toJsonStr(datacenterIds));
    }

    @PreDestroy
    @Override
    public void destroy() {
        AbsBean.super.destroy();
        log.debug("==> 销毁ApplicationUtil <==");
        //下线
        destroyApplicationIdAndDatacenterId();
    }

    /**
     * 销毁应用ID和数据中心的公共方法
     * 该方法调用重载的destroyApplicationIdAndDatacenterId方法，使用当前实例的applicationId和datacenterId作为参数
     */
    public void destroyApplicationIdAndDatacenterId() {
        // 调用重载方法，传入当前实例的applicationId和datacenterId参数
        destroyApplicationIdAndDatacenterId(getApplicationId(), getDatacenterId());
    }

    /**
     * 销毁应用ID和数据中心ID
     * 该方法会分别调用destroyApplicationId和destroyDatacenterId方法来清理相关资源
     *
     * @param applicationId 要销毁的应用ID
     * @param datacenterId  要销毁的数据中心ID
     */
    public static void destroyApplicationIdAndDatacenterId(String applicationId, Long datacenterId) {
        // 调用destroyApplicationId方法销毁应用ID
        destroyApplicationId(applicationId);
        // 调用destroyDatacenterId方法销毁数据中心ID
        destroyDatacenterId(datacenterId);
    }

    /**
     * 清理应用程序ID的方法
     * 从缓存中移除指定的应用程序ID，并更新缓存
     */
    public void destroyApplicationId() {
        destroyApplicationId(getApplicationId());
    }

    /**
     * 清理应用程序ID的方法
     * 从缓存中移除指定的应用程序ID，并更新缓存
     */
    public static void destroyApplicationId(String applicationId) {
        // 通过Spring工具类获取CacheService的Bean实例
        CacheService cacheService = SpringUtil.getBean(CacheService.class);
        // 根据application_key查找缓存
        Cache<String> cache = cacheService.find(application_key);
        // 创建一个ArrayList用于存储处理后的ID列表
        List<String> list = new ArrayList<>();
        // 检查缓存及其数据是否存在
        if (cache != null && cache.getData() != null) {
            // 将缓存中的JSON数据转换为String类型的List
            List<String> ids = JSONUtil.toList(cache.getData(), String.class);
            // 使用Stream过滤掉与当前applicationId相同的元素，并去重
            list = ids.stream().filter(e -> !ObjectUtils.equals(e, applicationId)).distinct().collect(Collectors.toList());
        }
        // 将处理后的List转换为JSON字符串并保存到缓存中
        cacheService.save(application_key, JSONUtil.toJsonStr(list));
    }

    /**
     * 销毁数据中心ID的方法
     * 该方法用于从缓存中移除指定的数据中心ID
     */
    public static void destroyDatacenterId() {
        destroyDatacenterId(getDatacenterId());
    }

    /**
     * 销毁指定ID的数据中心
     *
     * @param datacenterId 要销毁的数据中心ID
     */
    public static void destroyDatacenterId(Long datacenterId) {
        // 从Spring上下文中获取CacheService的Bean实例
        CacheService cacheService = SpringUtil.getBean(CacheService.class);
        // 根据键从缓存中获取数据中心数据
        String datacenters = cacheService.findById(application_datacenter_key);
        // 使用LinkedHashSet来存储数据中心ID，保证顺序且不重复
        LinkedHashSet<Long> datacenterIds = new LinkedHashSet<>();
        // 检查从缓存获取的数据是否为空
        if (StrUtil.isNotBlank(datacenters)) {
            // 检查数据是否为JSON数组格式
            if (JSONUtil.isTypeJSONArray(datacenters)) {
                JSONUtil.toList(datacenters, String.class).stream().filter(ObjectUtils::isNotEmpty).map(Long::valueOf).forEach(datacenterIds::add);
            } else {
                datacenterIds.add(Long.valueOf(datacenters));
            }
        }
        datacenterIds.remove(datacenterId);
        cacheService.save(application_datacenter_key, JSONUtil.toJsonStr(datacenterIds));
    }

    public static String getApplicationId() {
        return applicationInfo.getApplicationId();
    }

    public static Long getDatacenterId() {
        return applicationInfo.getDatacenterId();
    }

    public static List<String> getAllApplicationIds() {
        List<String> list = new ArrayList<>();
        Cache<String> cache = SpringUtil.getBean(CacheService.class).find(application_key);
        if (cache != null && cache.getData() != null) {
            List<String> ids = JSONUtil.toList(cache.getData(), String.class);
            list.addAll(ids);
        }
        //List<String> list = new ArrayList<>();
        //list.add(applicationId);
        //list.addAll(nodeApplicationIds);
        list = list.stream().filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        return list;
    }
}
