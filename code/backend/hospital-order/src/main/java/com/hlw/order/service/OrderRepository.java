package com.hlw.order.service;

/**
 * 订单仓储接口。
 */
public interface OrderRepository {
    /**
     * 按订单编号查询订单。
     *
     * @param orderId 订单编号
     * @return 订单对象
     */
    Order findById(Long orderId);

    /**
     * 保存订单。
     *
     * @param order 订单对象
     */
    void save(Order order);
}
