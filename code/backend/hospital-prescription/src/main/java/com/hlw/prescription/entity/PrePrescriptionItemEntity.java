package com.hlw.prescription.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 处方药品明细持久化对象。
 */
@Getter
@Setter
@TableName("pre_prescription_item")
public class PrePrescriptionItemEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 处方编号。 */
    private Long prescriptionId;
    /** 药品编号。 */
    private Long drugId;
    /** 药品名称。 */
    private String drugName;
    /** 剂量。 */
    private String dosage;
    /** 频次。 */
    private String frequency;
    /** 数量。 */
    private BigDecimal quantity;
    /** 用药备注。 */
    private String usageNote;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
    /** 创建人编号。 */
    private Long createBy;
    /** 更新人编号。 */
    private Long updateBy;
    /** 逻辑删除标识。 */
    @TableLogic
    private Integer deleted;
}
