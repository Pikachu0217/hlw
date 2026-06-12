package com.hlw.order.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 模拟支付服务测试。
 */
class MockPaymentServiceTest {
    /**
     * 验证模拟支付会更新订单状态并发送支付事件。
     */
    @Test
    void mock_pay_marks_order_paid_and_publishes_event() {
        RecordingMqProducer producer = new RecordingMqProducer();
        MockPaymentService service = new MockPaymentService(new InMemoryOrderRepository(), producer);

        Order order = service.pay(1L, "MOCK_PAY");

        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.payMethod()).isEqualTo("MOCK_PAY");
        assertThat(producer.lastTopic()).isEqualTo("order.paid");
    }
}
