package com.hlw.common.mq.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * 本地消费者注册表。
 */
public class LocalMqConsumerRegistry implements MqConsumerRegistry {
    // 主题与消费者列表映射。
    private final Map<String, List<Consumer<String>>> consumers = new ConcurrentHashMap<>();

    /**
     * 注册指定主题的消费者。
     *
     * @param topic 消息主题
     * @param consumer 消费者
     */
    @Override
    public void register(String topic, Consumer<String> consumer) {
        consumers.computeIfAbsent(topic, ignored -> new CopyOnWriteArrayList<>()).add(consumer);
    }

    /**
     * 查询指定主题的消费者。
     *
     * @param topic 消息主题
     * @return 消费者列表
     */
    @Override
    public List<Consumer<String>> consumers(String topic) {
        return consumers.getOrDefault(topic, List.of());
    }
}
