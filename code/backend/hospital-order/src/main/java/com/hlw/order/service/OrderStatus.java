package com.hlw.order.service;

/**
 * 订单状态枚举。
 */
public enum OrderStatus {
    /** 待支付。 */
    PENDING_PAY,
    /** 已支付。 */
    PAID,
    /** 已关闭。 */
    CLOSED,
    /** 已退款。 */
    REFUNDED
}
