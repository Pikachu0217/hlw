package com.hlw.common.mq.service.producer;

import com.hlw.common.core.exception.BizException;
import com.hlw.common.mq.enums.MessageBroadChannelEnum;
import com.hlw.common.mq.enums.MessageQueueEnum;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

/**
 * 消息队列对外统一入口，按配置切换底层实现。
 *
 * @param <T> 消息类型
 * @param <P> 优先级/延迟时间类型
 */
@RefreshScope
@Service
public class MessageQueueProducer<T, P> {

    private static final String QUEUE_TYPE_ROCKETMQ = "rocketmq";

    private static final String QUEUE_TYPE_REDIS = "redis";

    @Autowired
    private RedisMessageQueueSender<T, P> redisMessageQueueSender;

    @Autowired
    private ObjectProvider<RocketMessageQueueSender<T, P>> rocketMessageQueueSenderProvider;

    /**
     * 消息队列开关，默认 redis。
     */
    @Value("${message-queue.used:redis}")
    private String messageQueueUsed;

    /**
     * 普通队列消息发送。
     *
     * @param queue 队列
     * @param message 消息
     * @return 是否成功
     */
    public boolean send(MessageQueueEnum queue, T message) {
        return this.getSender().send(queue, message);
    }

    /**
     * 优先级队列消息发送。
     *
     * @param queue 队列
     * @param message 消息
     * @param priority 优先级/延迟时间
     * @return 是否成功
     */
    public boolean prioritySend(MessageQueueEnum queue, T message, P priority) {
        return this.getSender().prioritySend(queue, message, priority);
    }

    /**
     * 广播消息发送。
     *
     * @param channel 广播频道
     * @param message 消息
     */
    public void broadSend(MessageBroadChannelEnum channel, T message) {
        this.getSender().broadSend(channel, message);
    }

    private AbstractMessageQueueSender<T, P> getSender() {
        if (QUEUE_TYPE_ROCKETMQ.equals(messageQueueUsed)) {
            RocketMessageQueueSender<T, P> rocketSender = rocketMessageQueueSenderProvider.getIfAvailable();
            if (rocketSender == null) {
                throw new BizException(500, "未引入 RocketMQ 依赖或未启用 RocketMQ 配置");
            }
            return rocketSender;
        }
        if (QUEUE_TYPE_REDIS.equals(messageQueueUsed)) {
            return redisMessageQueueSender;
        }
        throw new BizException(500, "不支持的队列类型！");
    }
}
