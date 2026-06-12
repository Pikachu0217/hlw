package com.hlw.drug.config;

import com.hlw.common.mq.core.MqProducer;
import com.hlw.drug.service.DrugDeliveryService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 药品模块本地启动默认配置。
 */
@Configuration
public class DrugServiceConfig {
    /**
     * 创建药品配送服务。
     *
     * @param mqProducer 消息生产者
     * @return 药品配送服务
     */
    @Bean
    public DrugDeliveryService drugDeliveryService(MqProducer mqProducer) {
        return new DrugDeliveryService(mqProducer);
    }
}
