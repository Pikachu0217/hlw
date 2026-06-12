package com.hlw.order.service;

import com.hlw.common.mq.core.MqProducer;
import com.hlw.common.mq.model.MqMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模拟支付服务，负责更新订单状态并发送支付成功事件。
 */
public class MockPaymentService {
    private static final Logger log = LoggerFactory.getLogger(MockPaymentService.class);

    private final OrderRepository orderRepository;
    private final MqProducer mqProducer;

    /**
     * 构造模拟支付服务。
     *
     * @param orderRepository 订单仓储
     * @param mqProducer 消息生产者
     */
    public MockPaymentService(OrderRepository orderRepository, MqProducer mqProducer) {
        this.orderRepository = orderRepository;
        this.mqProducer = mqProducer;
    }

    /**
     * 执行模拟支付。
     *
     * @param orderId 订单编号
     * @param payMethod 支付方式
     * @return 支付后的订单
     */
    public Order pay(Long orderId, String payMethod) {
        log.info("模拟支付开始，orderId={}, payMethod={}", orderId, payMethod);
        Order order = orderRepository.findById(orderId).paid(payMethod);
        orderRepository.save(order);
        mqProducer.publish(new MqMessage("order.paid", "{\"orderId\":" + orderId + "}", 0, 0, 3));
        log.info("模拟支付完成并发布支付事件，orderId={}", orderId);
        return order;
    }
}
