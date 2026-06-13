package com.hlw.common.redis.lock;

import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisLockServiceConfig {
    /**
     * 创建 Redis 锁服务。
     *
     * @param redissonClient Redisson 客户端
     * @return Redis 锁服务
     */
    @Bean
    public RedisLockService redisLockService(RedissonClient redissonClient) {
        return new RedisLockService(redissonClient);
    }
}
