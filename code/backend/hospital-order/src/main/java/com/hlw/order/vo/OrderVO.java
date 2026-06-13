package com.hlw.order.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 订单展示对象。
 */
@Getter
@Setter
public class OrderVO {
    /** 表格主键。 */
    private String key;
    /** 订单编号。 */
    private Long id;
    /** 订单号。 */
    private String orderNo;
    /** 业务类型。 */
    private String businessType;
    /** 患者姓名。 */
    private String patientName;
    /** 订单金额。 */
    private String amount;
    /** 支付状态。 */
    private String payStatus;
    /** 创建时间展示值。 */
    private String createdAt;
}
