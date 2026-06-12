package com.hlw.common.redis.lock;

import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisLockServiceConfig {
    @Bean
    public RedisLockService redisLockService(RedissonClient redissonClient) {
        return new RedisLockService(redissonClient);
    }
}
