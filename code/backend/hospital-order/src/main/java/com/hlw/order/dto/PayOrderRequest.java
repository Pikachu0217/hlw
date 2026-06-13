package com.hlw.order.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 支付订单请求。
 */
@Getter
@Setter
public class PayOrderRequest {
    /** 支付方式。 */
    private String payMethod;
}
