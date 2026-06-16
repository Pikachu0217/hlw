package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * 本地消息分发器。
 */
@Slf4j
public class LocalMqDispatcher implements MqDispatcher {

    // 消费者注册表。
    private final MqConsumerRegistry mqConsumerRegistry;

    /**
     * 构造本地消息分发器。
     *
     * @param mqConsumerRegistry 消费者注册表
     */
    public LocalMqDispatcher(MqConsumerRegistry mqConsumerRegistry) {
        this.mqConsumerRegistry = mqConsumerRegistry;
    }

    /**
     * 分发消息到已注册消费者。
     *
     * @param message 消息对象
     */
    @Override
    public void dispatch(MqMessage message) {
        List<Consumer<String>> consumers = mqConsumerRegistry.consumers(message.getTopic());
        log.info("本地消息分发，topic={}, consumerCount={}", message.getTopic(), consumers.size());
        consumers.forEach(consumer -> consumer.accept(message.getBody()));
    }
}
