package com.hlw.consult.config;

import com.hlw.consult.service.ConsultDurationProvider;
import com.hlw.consult.service.ConsultLifecycleService;
import com.hlw.consult.service.ConsultRepository;
import com.hlw.consult.service.ConsultTimeoutScheduler;
import com.hlw.consult.service.ConsultTimeoutWarningPublisher;
import com.hlw.consult.service.InMemoryConsultRepository;
import com.hlw.consult.ws.ConsultMessageHandler;
import com.hlw.consult.ws.ConsultMessageRepository;
import com.hlw.consult.ws.ConsultWebSocketEndpoint;
import com.hlw.consult.ws.JdbcConsultMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

/**
 * 问诊模块本地启动默认配置。
 */
@Configuration
public class ConsultServiceConfig {
    private static final Logger log = LoggerFactory.getLogger(ConsultServiceConfig.class);

    /**
     * 创建问诊生命周期服务。
     *
     * @param consultRepository 问诊仓储
     * @param consultDurationProvider 问诊时长提供器
     * @return 问诊生命周期服务
     */
    @Bean
    public ConsultLifecycleService consultLifecycleService(
        ConsultRepository consultRepository,
        ConsultDurationProvider consultDurationProvider
    ) {
        return new ConsultLifecycleService(consultRepository, consultDurationProvider);
    }

    /**
     * 创建问诊超时调度器。
     *
     * @param consultRepository 问诊仓储
     * @param consultTimeoutWarningPublisher 超时提醒发布器
     * @return 问诊超时调度器
     */
    @Bean
    public ConsultTimeoutScheduler consultTimeoutScheduler(
        ConsultRepository consultRepository,
        ConsultTimeoutWarningPublisher consultTimeoutWarningPublisher
    ) {
        return new ConsultTimeoutScheduler(consultRepository, consultTimeoutWarningPublisher);
    }

    /**
     * 创建默认问诊时长提供器。
     *
     * @return 问诊时长提供器
     */
    @Bean
    public ConsultDurationProvider consultDurationProvider() {
        return tenantId -> 30;
    }

    /**
     * 创建问诊超时提醒发布器。
     *
     * @return 问诊超时提醒发布器
     */
    @Bean
    public ConsultTimeoutWarningPublisher consultTimeoutWarningPublisher() {
        return consultId -> log.info("问诊即将超时，consultId={}", consultId);
    }

    /**
     * 创建内存问诊仓储。
     *
     * @return 问诊仓储
     */
    @Bean
    public ConsultRepository consultRepository() {
        return new InMemoryConsultRepository();
    }

    /**
     * 创建问诊消息处理器。
     *
     * @param consultMessageRepository 问诊消息仓储
     * @return 问诊消息处理器
     */
    @Bean
    public ConsultMessageHandler consultMessageHandler(ConsultMessageRepository consultMessageRepository) {
        return new ConsultMessageHandler(consultMessageRepository);
    }

    /**
     * 创建问诊 WebSocket 端点。
     *
     * @param consultMessageHandler 问诊消息处理器
     * @return 问诊 WebSocket 端点
     */
    @Bean
    public ConsultWebSocketEndpoint consultWebSocketEndpoint(ConsultMessageHandler consultMessageHandler) {
        return new ConsultWebSocketEndpoint(consultMessageHandler);
    }

    /**
     * 创建 JDBC 问诊消息仓储。
     *
     * @param jdbcOperations JDBC 操作组件
     * @return 问诊消息仓储
     */
    @Bean
    public ConsultMessageRepository consultMessageRepository(JdbcOperations jdbcOperations) {
        return new JdbcConsultMessageRepository(jdbcOperations);
    }
}
