package com.hlw.common.mq.config;

import com.hlw.common.mq.core.MqConsumerRegistry;
import com.hlw.common.mq.core.MqDispatcher;
import com.hlw.common.mq.core.MqLocalMessageStore;
import com.hlw.common.mq.core.MqProducer;
import com.hlw.common.mq.model.MqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

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
     * @param jdbcOperationsProvider JDBC 操作组件提供器
     * @return 本地消息存储
     */
    @Bean
    public MqLocalMessageStore mqLocalMessageStore(ObjectProvider<JdbcOperations> jdbcOperationsProvider) {
        JdbcOperations jdbcOperations = jdbcOperationsProvider.getIfAvailable();
        if (jdbcOperations != null) {
            return new JdbcMqLocalMessageStore(jdbcOperations);
        }
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

    /**
     * JDBC 本地消息存储。
     */
    private static final class JdbcMqLocalMessageStore implements MqLocalMessageStore {
        private static final Logger log = LoggerFactory.getLogger(JdbcMqLocalMessageStore.class);
        private static final long DEFAULT_TENANT_ID = 100L;

        private final JdbcOperations jdbcOperations;
        private final boolean tenantIdColumnExists;
        private final boolean payloadColumnExists;
        private final boolean deletedColumnExists;

        /**
         * 构造 JDBC 本地消息存储。
         *
         * @param jdbcOperations JDBC 操作组件
         */
        private JdbcMqLocalMessageStore(JdbcOperations jdbcOperations) {
            this.jdbcOperations = jdbcOperations;
            this.tenantIdColumnExists = columnExists("tenant_id");
            this.payloadColumnExists = columnExists("payload");
            this.deletedColumnExists = columnExists("deleted");
        }

        /**
         * 保存待发送消息。
         *
         * @param message 消息对象
         */
        @Override
        public void save(MqMessage message) {
            log.info("保存本地消息，topic={}", message.topic());
            if (tenantIdColumnExists && payloadColumnExists) {
                jdbcOperations.update("""
                    INSERT INTO local_message (tenant_id, topic, payload, body, retry_count, status)
                    VALUES (?, ?, ?, ?, ?, 'PENDING')
                    """, DEFAULT_TENANT_ID, message.topic(), message.body(), message.body(), message.retryCount());
                return;
            }
            if (tenantIdColumnExists) {
                jdbcOperations.update("""
                    INSERT INTO local_message (tenant_id, topic, body, retry_count, status)
                    VALUES (?, ?, ?, ?, 'PENDING')
                    """, DEFAULT_TENANT_ID, message.topic(), message.body(), message.retryCount());
                return;
            }
            jdbcOperations.update("""
                INSERT INTO local_message (topic, body, retry_count, status)
                VALUES (?, ?, ?, 'PENDING')
                """, message.topic(), message.body(), message.retryCount());
        }

        /**
         * 查询待发送消息。
         *
         * @param limit 查询数量限制
         * @return 待发送消息列表
         */
        @Override
        public List<MqMessage> findPending(int limit) {
            log.info("查询待发送本地消息，limit={}", limit);
            String bodyExpression = payloadColumnExists ? "COALESCE(NULLIF(payload, ''), body)" : "body";
            String deletedFilter = deletedColumnExists ? " AND deleted = 0" : "";
            return jdbcOperations.query("""
                SELECT topic, %s AS body, retry_count
                FROM local_message
                WHERE status = 'PENDING'%s
                ORDER BY id
                LIMIT ?
                """.formatted(bodyExpression, deletedFilter), (resultSet, rowNum) -> new MqMessage(
                resultSet.getString("topic"),
                resultSet.getString("body"),
                0,
                resultSet.getInt("retry_count"),
                3
            ), limit);
        }

        /**
         * 标记消息已发送。
         *
         * @param message 消息对象
         */
        @Override
        public void markSent(MqMessage message) {
            log.info("标记本地消息已发送，topic={}", message.topic());
            String bodyExpression = payloadColumnExists ? "COALESCE(NULLIF(payload, ''), body)" : "body";
            String deletedFilter = deletedColumnExists ? " AND deleted = 0" : "";
            jdbcOperations.update("""
                UPDATE local_message
                SET status = 'SENT', update_time = CURRENT_TIMESTAMP
                WHERE id = (
                    SELECT id FROM local_message
                    WHERE status = 'PENDING'%s AND topic = ? AND %s = ?
                    ORDER BY id
                    LIMIT 1
                )
                """.formatted(deletedFilter, bodyExpression), message.topic(), message.body());
        }

        /**
         * 标记消息发送失败。
         *
         * @param message 消息对象
         * @param errorMessage 错误信息
         */
        @Override
        public void markFailed(MqMessage message, String errorMessage) {
            log.warn("标记本地消息发送失败，topic={}，error={}", message.topic(), errorMessage);
            String bodyExpression = payloadColumnExists ? "COALESCE(NULLIF(payload, ''), body)" : "body";
            String deletedFilter = deletedColumnExists ? " AND deleted = 0" : "";
            jdbcOperations.update("""
                UPDATE local_message
                SET status = 'FAILED', retry_count = ?, update_time = CURRENT_TIMESTAMP
                WHERE id = (
                    SELECT id FROM local_message
                    WHERE status = 'PENDING'%s AND topic = ? AND %s = ?
                    ORDER BY id
                    LIMIT 1
                )
                """.formatted(deletedFilter, bodyExpression), message.retryCount() + 1, message.topic(), message.body());
        }

        /**
         * 判断本地消息表是否存在指定字段，兼容两份初始化脚本的字段差异。
         *
         * @param columnName 字段名
         * @return 是否存在
         */
        private boolean columnExists(String columnName) {
            Integer count = jdbcOperations.queryForObject("""
                SELECT COUNT(1)
                FROM information_schema.columns
                WHERE table_schema = current_schema() AND table_name = 'local_message' AND column_name = ?
                """, Integer.class, columnName);
            return count != null && count > 0;
        }
    }
}
