package com.hlw.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 退款成功回调请求。
 */
@Getter
@Setter
public class RefundCallbackRequest {
    /** 订单号。 */
    private String orderNo;
    /** 退款时间。 */
    private LocalDateTime refundTime;
    /** 第三方退款交易号。 */
    private String tradeNo;
    /** 退款金额。 */
    private BigDecimal refundAmount;
}
