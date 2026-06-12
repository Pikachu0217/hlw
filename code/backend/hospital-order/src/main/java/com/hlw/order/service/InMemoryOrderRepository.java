package com.hlw.order.service;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于内存的订单仓储实现。
 */
public class InMemoryOrderRepository implements OrderRepository {
    private final Map<Long, Order> orders = new HashMap<>();

    /**
     * 按订单编号查询订单，不存在时返回默认待支付订单。
     *
     * @param orderId 订单编号
     * @return 订单对象
     */
    @Override
    public Order findById(Long orderId) {
        return orders.getOrDefault(orderId, new Order(orderId, OrderStatus.PENDING_PAY, null));
    }

    /**
     * 保存订单快照。
     *
     * @param order 订单对象
     */
    @Override
    public void save(Order order) {
        orders.put(order.id(), order);
    }
}
