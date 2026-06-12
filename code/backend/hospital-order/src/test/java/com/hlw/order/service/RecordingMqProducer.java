package com.hlw.order.service;

import com.hlw.common.mq.core.MqProducer;
import com.hlw.common.mq.model.MqMessage;

/**
 * 测试用消息生产者，用于断言支付事件是否发送。
 */
class RecordingMqProducer implements MqProducer {
    private String lastTopic;

    /**
     * 记录最后一次发送的消息主题。
     *
     * @param message 消息对象
     */
    @Override
    public void publish(MqMessage message) {
        lastTopic = message.topic();
    }

    /**
     * 返回最后一次发送的主题。
     *
     * @return 消息主题
     */
    String lastTopic() {
        return lastTopic;
    }
}
