package com.hlw.common.mq.core;

import java.util.List;
import java.util.function.Consumer;

/**
 * 消费者注册表。
 */
public interface MqConsumerRegistry {
    /**
     * 注册指定主题的消费者。
     *
     * @param topic 消息主题
     * @param consumer 消费者
     */
    void register(String topic, Consumer<String> consumer);

    /**
     * 查询指定主题的消费者。
     *
     * @param topic 消息主题
     * @return 消费者列表
     */
    List<Consumer<String>> consumers(String topic);
}
