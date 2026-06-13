package com.hlw.prescription.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 处方持久化对象。
 */
@Getter
@Setter
@TableName("pre_prescription")
public class PrePrescriptionEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 问诊编号。 */
    private Long consultId;
    /** 患者编号。 */
    private Long patientId;
    /** 医生编号。 */
    private Long doctorId;
    /** 审核药师编号。 */
    private Long pharmacistId;
    /** 处方编号。 */
    private String prescriptionNo;
    /** 患者姓名。 */
    private String patientName;
    /** 医生姓名。 */
    private String doctorName;
    /** 药品数量。 */
    private Integer drugCount;
    /** 开方时间展示值。 */
    private String issuedAt;
    /** 处方状态。 */
    private String status;
    /** 审核备注。 */
    private String auditRemark;
    /** 提交时间。 */
    private LocalDateTime submitTime;
    /** 审核时间。 */
    private LocalDateTime auditTime;
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
