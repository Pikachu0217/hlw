package com.hlw.order.service;

/**
 * 订单聚合根。
 *
 * @param id 订单编号
 * @param status 订单状态
 * @param payMethod 支付方式
 */
public record Order(Long id, OrderStatus status, String payMethod) {
    /**
     * 生成支付成功后的订单快照。
     *
     * @param payMethod 支付方式
     * @return 支付成功后的订单
     */
    public Order paid(String payMethod) {
        return new Order(id, OrderStatus.PAID, payMethod);
    }
}
