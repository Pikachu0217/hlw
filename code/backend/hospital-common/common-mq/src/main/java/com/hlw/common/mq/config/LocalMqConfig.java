package com.hlw.common.mq.config;

import com.hlw.common.mq.core.MqConsumerRegistry;
import com.hlw.common.mq.core.MqDispatcher;
import com.hlw.common.mq.core.MqLocalMessageStore;
import com.hlw.common.mq.core.MqProducer;
import com.hlw.common.mq.model.MqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 本地消息队列默认配置。
 */
@Configuration
public class LocalMqConfig {
    /**
     * 创建本地消息生产者。
     *
     * @param mqLocalMessageStore 本地消息存储
     * @param mqDispatcher 消息分发器
     * @return 消息生产者
     */
    @Bean
    public MqProducer mqProducer(MqLocalMessageStore mqLocalMessageStore, MqDispatcher mqDispatcher) {
        return message -> {
            mqLocalMessageStore.save(message);
            mqDispatcher.dispatch(message);
        };
    }

    /**
     * 创建本地消息分发器。
     *
     * @param mqConsumerRegistry 消费者注册表
     * @return 消息分发器
     */
    @Bean
    public MqDispatcher mqDispatcher(MqConsumerRegistry mqConsumerRegistry) {
        return new LocalMqDispatcher(mqConsumerRegistry);
    }

    /**
     * 创建消费者注册表。
     *
     * @return 消费者注册表
     */
    @Bean
    public MqConsumerRegistry mqConsumerRegistry() {
        return new LocalMqConsumerRegistry();
    }

    /**
     * 创建本地消息存储。
     *
     * @return 本地消息存储
     */
    @Bean
    public MqLocalMessageStore mqLocalMessageStore() {
        return new InMemoryMqLocalMessageStore();
    }

    /**
     * 内存消费者注册表。
     */
    private static final class LocalMqConsumerRegistry implements MqConsumerRegistry {
        private final Map<String, List<Consumer<String>>> consumers = new ConcurrentHashMap<>();

        /**
         * 注册指定主题的消费者。
         *
         * @param topic 消息主题
         * @param consumer 消费者
         */
        @Override
        public void register(String topic, Consumer<String> consumer) {
            consumers.computeIfAbsent(topic, ignored -> new ArrayList<>()).add(consumer);
        }

        /**
         * 查询指定主题的消费者。
         *
         * @param topic 消息主题
         * @return 消费者列表
         */
        private List<Consumer<String>> consumers(String topic) {
            return consumers.getOrDefault(topic, List.of());
        }
    }

    /**
     * 本地消息分发器。
     */
    private static final class LocalMqDispatcher implements MqDispatcher {
        private static final Logger log = LoggerFactory.getLogger(LocalMqDispatcher.class);

        private final MqConsumerRegistry mqConsumerRegistry;

        /**
         * 构造本地消息分发器。
         *
         * @param mqConsumerRegistry 消费者注册表
         */
        private LocalMqDispatcher(MqConsumerRegistry mqConsumerRegistry) {
            this.mqConsumerRegistry = mqConsumerRegistry;
        }

        /**
         * 分发消息到已注册消费者。
         *
         * @param message 消息对象
         */
        @Override
        public void dispatch(MqMessage message) {
            if (mqConsumerRegistry instanceof LocalMqConsumerRegistry registry) {
                List<Consumer<String>> consumers = registry.consumers(message.topic());
                log.info("本地消息分发，topic={}, consumerCount={}", message.topic(), consumers.size());
                consumers.forEach(consumer -> consumer.accept(message.body()));
                return;
            }
            log.info("本地消息已接收，topic={}", message.topic());
        }
    }

    /**
     * 内存本地消息存储。
     */
    private static final class InMemoryMqLocalMessageStore implements MqLocalMessageStore {
        private final List<MqMessage> pendingMessages = new ArrayList<>();

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
            pendingMessages.add(new MqMessage(
                message.topic(),
                message.body(),
                message.delayMillis(),
                message.retryCount() + 1,
                message.maxRetry()
            ));
        }
    }
}
