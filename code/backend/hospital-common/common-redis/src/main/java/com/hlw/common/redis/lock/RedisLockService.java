package com.hlw.common.redis.lock;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

public class RedisLockService {
    private final RedissonClient redissonClient;

    public RedisLockService(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 尝试获取 Redis 分布式锁。
     *
     * @param key 锁键
     * @param waitTime 等待时间
     * @param leaseTime 持有时间
     * @param unit 时间单位
     * @return 是否获取成功
     * @throws InterruptedException 等待锁被中断时抛出
     */
    public boolean tryLock(String key, long waitTime, long leaseTime, TimeUnit unit) throws InterruptedException {
        return redissonClient.getLock(key).tryLock(waitTime, leaseTime, unit);
    }

    /**
     * 释放当前线程持有的 Redis 分布式锁。
     *
     * @param key 锁键
     */
    public void unlock(String key) {
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }
}
