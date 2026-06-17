package com.hlw.common.mq.service.producer;

import com.hlw.common.core.util.JsonUtil;
import com.hlw.common.mq.enums.MessageBroadChannelEnum;
import com.hlw.common.mq.enums.MessageQueueEnum;

/**
 * 消息队列发送者抽象基类，统一消息序列化与方法分派。
 *
 * @param <T> 消息类型
 * @param <P> 优先级/延迟时间类型
 */
public abstract class AbstractMessageQueueSender<T, P> implements MessageQueueSender<T, P> {

    /**
     * 默认 JSON 序列化，String 直接返回。
     *
     * @param message 消息对象
     * @return 序列化结果
     */
    protected String serializeMessage(T message) {
        if (message instanceof String) {
            return message.toString();
        }
        return JsonUtil.toJsonString(message);
    }

    /**
     * 实际发送逻辑由子类实现。
     *
     * @param queue topic
     * @param message 消息
     * @return 是否成功
     */
    protected abstract boolean doSend(String queue, T message);

    /**
     * 广播实现由子类实现。
     *
     * @param channel 频道
     * @param message 消息
     * @return 是否成功
     */
    protected abstract boolean broadSend(String channel, T message);

    /**
     * 优先级/延迟发送实现由子类实现。
     *
     * @param channel topic
     * @param message 消息
     * @param priority Redis 传未来某个时刻的毫秒时间戳，RocketMQ 传延迟秒数
     * @return 是否成功
     */
    protected abstract boolean prioritySend(String channel, T message, P priority);

    @Override
    public boolean send(MessageQueueEnum queue, T message) {
        return this.doSend(queue.getQueue(), message);
    }

    @Override
    public boolean prioritySend(MessageQueueEnum queue, T message, P priority) {
        return this.prioritySend(queue.getQueue(), message, priority);
    }

    @Override
    public void broadSend(MessageBroadChannelEnum channel, T message) {
        this.broadSend(channel.getChannel(), message);
    }
}
