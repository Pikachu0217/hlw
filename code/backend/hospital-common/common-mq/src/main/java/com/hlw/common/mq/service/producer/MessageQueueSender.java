package com.hlw.common.mq.service.producer;

import com.hlw.common.mq.enums.MessageBroadChannelEnum;
import com.hlw.common.mq.enums.MessageQueueEnum;

/**
 * 消息队列发送者抽象接口。
 *
 * @param <T> 消息类型
 * @param <P> 优先级/延迟时间类型
 */
public interface MessageQueueSender<T, P> {

    /**
     * 普通队列消息发送。
     *
     * @param queue 队列
     * @param message 消息
     * @return 是否成功
     */
    boolean send(MessageQueueEnum queue, T message);

    /**
     * 优先级/延迟队列消息发送。
     *
     * @param queue 队列
     * @param message 消息
     * @param priority 优先级或延迟时间
     * @return 是否成功
     */
    boolean prioritySend(MessageQueueEnum queue, T message, P priority);

    /**
     * 广播消息发送。
     *
     * @param channel 广播频道
     * @param message 消息
     */
    void broadSend(MessageBroadChannelEnum channel, T message);
}
