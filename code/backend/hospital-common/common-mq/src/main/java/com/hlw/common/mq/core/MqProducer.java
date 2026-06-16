package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;

/**
 * 消息生产者。
 */
public interface MqProducer {
    /**
     * 发布消息。
     *
     * @param message 消息对象
     */
    void publish(MqMessage message);
}
