package com.hlw.drug.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 药品配送持久化对象。
 */
@Getter
@Setter
@TableName("drug_delivery")
public class DrugDeliveryEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 订单编号。 */
    private Long orderId;
    /** 处方编号。 */
    private Long prescriptionId;
    /** 配送状态。 */
    private String status;
    /** 收货人姓名。 */
    private String receiverName;
    /** 收货人电话。 */
    private String receiverPhone;
    /** 收货地址。 */
    private String receiverAddress;
    /** 物流单号。 */
    private String trackingNo;
    /** 发货时间。 */
    private LocalDateTime shipTime;
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
