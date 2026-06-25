package com.hlw.order.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付成功回调请求。
 */
@Getter
@Setter
public class PayCallbackRequest {
    /** 订单号。 */
    private String orderNo;
    /** 支付时间。 */
    private LocalDateTime payTime;
    /** 第三方交易号。 */
    private String tradeNo;
    /** 支付金额。 */
    private BigDecimal payAmount;
    /** 支付方式。 */
    private String payMethod;
}
