package com.hlw.order.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 创建订单请求。
 */
@Getter
@Setter
public class CreateOrderRequest {
    /** 业务类型编码。 */
    private String bizType;
    /** 兼容前端旧字段的业务类型编码。 */
    private String businessType;
    /** 业务单据编号。 */
    private Long bizId;
    /** 患者编号。 */
    private Long patientId;
    /** 患者姓名。 */
    private String patientName;
    /** 订单金额。 */
    @DecimalMin(value = "0", message = "订单金额不能小于 0")
    private BigDecimal amount;
    /** 创建时间展示值。 */
    private String createdAt;
}
