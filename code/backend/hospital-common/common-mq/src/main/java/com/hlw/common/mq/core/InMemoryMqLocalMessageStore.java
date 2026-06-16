package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;
import com.hlw.common.mq.model.DelayMqMessage;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 内存本地消息存储（线程安全）。
 */
public class InMemoryMqLocalMessageStore implements MqLocalMessageStore {
    // 待发送消息列表。
    private final List<MqMessage> pendingMessages = new CopyOnWriteArrayList<>();

    /**
     * 保存待发送消息。
     *
     * @param message 消息对象
     */
    @Override
    public void save(MqMessage message) {
        pendingMessages.add(message);
    }

    /**
     * 查询待发送消息。
     *
     * @param limit 查询数量限制
     * @return 待发送消息列表
     */
    @Override
    public List<MqMessage> findPending(int limit) {
        return pendingMessages.stream().limit(limit).toList();
    }

    /**
     * 标记消息已发送。
     *
     * @param message 消息对象
     */
    @Override
    public void markSent(MqMessage message) {
        pendingMessages.remove(message);
    }

    /**
     * 标记消息发送失败。
     *
     * @param message 消息对象
     * @param errorMessage 错误信息
     */
    @Override
    public void markFailed(MqMessage message, String errorMessage) {
        pendingMessages.remove(message);
        pendingMessages.add(retryMessage(message));
    }

    /**
     * 构造下一次重试消息。
     *
     * @param message 原始消息对象
     * @return 下一次重试消息
     */
    private MqMessage retryMessage(MqMessage message) {
        if (message instanceof DelayMqMessage delayMqMessage) {
            return new DelayMqMessage(
                delayMqMessage.getTopic(),
                delayMqMessage.getBody(),
                delayMqMessage.getDelayMillis(),
                delayMqMessage.getRetryCount() + 1,
                delayMqMessage.getMaxRetry()
            );
        }
        return new MqMessage(
            message.getTopic(),
            message.getBody(),
            message.getRetryCount() + 1,
            message.getMaxRetry()
        );
    }
}
