package com.hlw.prescription.config;

import com.hlw.common.mq.core.MqProducer;
import com.hlw.prescription.service.InMemoryPrescriptionRepository;
import com.hlw.prescription.service.PrescriptionAuditService;
import com.hlw.prescription.service.PrescriptionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 处方模块本地启动默认配置。
 */
@Configuration
public class PrescriptionServiceConfig {
    /**
     * 创建处方审核服务。
     *
     * @param prescriptionRepository 处方仓储
     * @param mqProducer 消息生产者
     * @return 处方审核服务
     */
    @Bean
    public PrescriptionAuditService prescriptionAuditService(
        PrescriptionRepository prescriptionRepository,
        MqProducer mqProducer
    ) {
        return new PrescriptionAuditService(prescriptionRepository, mqProducer);
    }

    /**
     * 创建内存处方仓储。
     *
     * @return 处方仓储
     */
    @Bean
    public PrescriptionRepository prescriptionRepository() {
        return new InMemoryPrescriptionRepository();
    }
}
