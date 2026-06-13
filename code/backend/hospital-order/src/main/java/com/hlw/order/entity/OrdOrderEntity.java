package com.hlw.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单持久化对象。
 */
@Getter
@Setter
@TableName("ord_order")
public class OrdOrderEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 订单号。 */
    private String orderNo;
    /** 业务类型编码。 */
    private String bizType;
    /** 业务编号。 */
    private Long bizId;
    /** 患者编号。 */
    private Long patientId;
    /** 业务类型。 */
    private String businessType;
    /** 患者姓名。 */
    private String patientName;
    /** 订单金额。 */
    private BigDecimal amount;
    /** 订单状态。 */
    private String status;
    /** 支付状态。 */
    private String payStatus;
    /** 支付方式。 */
    private String payMethod;
    /** 支付时间。 */
    private LocalDateTime payTime;
    /** 订单创建时间展示值。 */
    private String createdAt;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
    /** 创建人编号。 */
    private Long createBy;
    /** 更新人编号。 */
    private Long updateBy;
    /** 逻辑删除标识。 */
    private Integer deleted;
}
