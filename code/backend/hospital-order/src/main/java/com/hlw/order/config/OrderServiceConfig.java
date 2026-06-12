package com.hlw.order.config;

import com.hlw.common.mq.core.MqProducer;
import com.hlw.order.service.InMemoryOrderRepository;
import com.hlw.order.service.MockPaymentService;
import com.hlw.order.service.OrderRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 订单模块本地启动默认配置。
 */
@Configuration
public class OrderServiceConfig {
    /**
     * 创建模拟支付服务。
     *
     * @param orderRepository 订单仓储
     * @param mqProducer 消息生产者
     * @return 模拟支付服务
     */
    @Bean
    public MockPaymentService mockPaymentService(OrderRepository orderRepository, MqProducer mqProducer) {
        return new MockPaymentService(orderRepository, mqProducer);
    }

    /**
     * 创建内存订单仓储。
     *
     * @return 订单仓储
     */
    @Bean
    public OrderRepository orderRepository() {
        return new InMemoryOrderRepository();
    }
}
