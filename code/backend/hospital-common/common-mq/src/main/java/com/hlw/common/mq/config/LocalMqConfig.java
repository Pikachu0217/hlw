package com.hlw.common.mq.config;

import com.hlw.common.mq.core.MqConsumerRegistry;
import com.hlw.common.mq.core.MqDispatcher;
import com.hlw.common.mq.core.InMemoryMqLocalMessageStore;
import com.hlw.common.mq.core.JdbcMqLocalMessageStore;
import com.hlw.common.mq.core.LocalMqConsumerRegistry;
import com.hlw.common.mq.core.LocalMqDispatcher;
import com.hlw.common.mq.core.MqLocalMessageStore;
import com.hlw.common.mq.core.MqProducer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

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
}
