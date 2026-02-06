package com.cloud_guest.service;

import com.cloud_guest.domain.Cache;

import java.util.List;

/**
 * @Author yan
 * @Date 2026/2/6 16:01:28
 * @Description
 */
public interface CacheService {
    boolean delList(List<String> ids);

    boolean save(String id, String json);

    Cache<String> find(String id);
}
