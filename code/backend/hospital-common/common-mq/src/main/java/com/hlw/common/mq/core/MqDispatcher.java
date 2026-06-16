package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;

/**
 * 消息分发器。
 */
public interface MqDispatcher {
    /**
     * 分发消息到消费者。
     *
     * @param message 消息对象
     */
    void dispatch(MqMessage message);
}
