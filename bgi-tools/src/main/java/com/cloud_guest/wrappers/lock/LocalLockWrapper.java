package com.cloud_guest.wrappers.lock;

/**
 * @Author yan
 * @Date 2026/2/28 21:23:37
 * @Description
 */

import cn.hutool.extra.spring.SpringUtil;
import com.cloud_guest.constants.KeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 本地锁实现
 */
@Slf4j
public class LocalLockWrapper extends AbstractLockWrapper {
    private final String lockKey;
    private final long waitTime;
    private final long leaseTime;
    private final TimeUnit timeUnit;
    private ReentrantLock reentrantLock;
    private volatile boolean locked = false;

    @Override
    public long getWaitTime() {
        return waitTime;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public LocalLockWrapper(String lockKey) {
        this(lockKey, DEFAULT_WAIT_TIME, DEFAULT_LEASE_TIME);
    }

    public LocalLockWrapper(String lockKey, long waitTime, long leaseTime) {
        this(lockKey, waitTime, leaseTime, DEFAULT_TIME_UNIT);
    }

    public LocalLockWrapper(String lockKey, long waitTime, long leaseTime, TimeUnit timeUnit) {
        this.waitTime = waitTime;
        this.leaseTime = leaseTime;
        this.lockKey = KeyConstants.local_lock_key + lockKey;
        this.timeUnit = timeUnit;
        this.reentrantLock = LOCAL_LOCKS.computeIfAbsent(lockKey, k -> new ReentrantLock());
    }

    @Override
    public boolean lock() {
        try {
            locked = reentrantLock.tryLock(waitTime, timeUnit);
            if (locked) {
                log.debug("本地锁获取成功: {}", lockKey);
            } else {
                log.warn("本地锁获取失败: {}", lockKey);
            }
            return locked;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("本地锁获取被中断: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public boolean tryLock() {
        return tryLock(waitTime, leaseTime, timeUnit);
    }

    @Override
    public boolean tryLock(long waitTime, TimeUnit timeUnit) {
        try {
            locked = reentrantLock.tryLock(waitTime, timeUnit);
            if (locked) {
                log.debug("本地锁尝试获取成功: {}", lockKey);
            }
            return locked;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("本地锁尝试获取被中断: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public boolean tryLock(long waitTime, long leaseTime, TimeUnit timeUnit) {
        try {
            locked = reentrantLock.tryLock(waitTime, timeUnit);
            if (locked) {
                log.debug("本地锁尝试获取成功: {}", lockKey);
            }
            return locked;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("本地锁尝试获取被中断: {}", lockKey, e);
            return false;
        }
    }

    @Override
    public void unlock() {
        if (locked && reentrantLock.isHeldByCurrentThread()) {
            reentrantLock.unlock();
            locked = false;
            log.debug("本地锁释放成功: {}", lockKey);
        }
    }

    @Override
    public boolean isLocked() {
        return locked && reentrantLock.isLocked();
    }
}