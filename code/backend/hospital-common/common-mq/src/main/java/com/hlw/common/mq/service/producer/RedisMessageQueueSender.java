package com.hlw.common.mq.service.producer;

import com.hlw.common.redis.service.RedisService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 基于 Redis 的消息队列发送实现。
 *
 * @param <T> 消息类型
 * @param <P> 优先级/延迟时间类型（毫秒时间戳）
 */
@Service
public class RedisMessageQueueSender<T, P> extends AbstractMessageQueueSender<T, P> {

    @Autowired
    private RedisService redisService;

    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    protected boolean doSend(String queue, T message) {
        String messageValue = super.serializeMessage(message);
        Long size = redisService.lLeftPush(queue, messageValue);
        return size != null && size > 0L;
    }

    @Override
    protected boolean broadSend(String channel, T message) {
        String messageValue = super.serializeMessage(message);
        redisTemplate.convertAndSend(channel, messageValue);
        return true;
    }

    @Override
    protected boolean prioritySend(String queue, T message, P priority) {
        String messageValue = super.serializeMessage(message);
        double score = Double.parseDouble(String.valueOf(priority));
        return redisService.zAdd(queue, messageValue, score);
    }
}
