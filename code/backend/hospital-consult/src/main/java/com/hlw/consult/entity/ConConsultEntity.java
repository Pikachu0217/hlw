package com.hlw.consult.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 问诊单持久化对象。
 */
@Getter
@Setter
@TableName("con_consult")
public class ConConsultEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 患者编号。 */
    private Long patientId;
    /** 医生编号。 */
    private Long doctorId;
    /** 关联预约单编号。 */
    private Long appointmentId;
    /** 问诊类型。 */
    private String consultType;
    /** 问诊单号。 */
    private String consultNo;
    /** 患者姓名。 */
    private String patientName;
    /** 医生姓名。 */
    private String doctorName;
    /** 问诊渠道。 */
    private String channel;
    /** 问诊状态。 */
    private String status;
    /** 支付状态。 */
    private String payStatus;
    /** 问诊费用。 */
    private BigDecimal feeAmount;
    /** 问诊时长上限分钟。 */
    private Integer durationLimit;
    /** 剩余问诊秒数。 */
    private Integer remainingSeconds;
    /** 接单开始时间。 */
    private LocalDateTime startTime;
    /** 问诊结束时间。 */
    private LocalDateTime endTime;
    /** 前端展示更新时间。 */
    private String updatedAt;
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
