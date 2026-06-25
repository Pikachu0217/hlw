package com.hlw.appointment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 预约单持久化对象。
 */
@Getter
@Setter
@TableName("apt_appointment")
public class AptAppointmentEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 患者编号。 */
    private Long patientId;
    /** 医生编号。 */
    private Long doctorId;
    /** 科室编号。 */
    private Long departmentId;
    /** 排班编号。 */
    private Long scheduleId;
    /** 号源编号。 */
    private Long numberSourceId;
    /** 预约类型。 */
    private String appointmentType;
    /** 预约单号。 */
    private String appointmentNo;
    /** 患者姓名。 */
    private String patientName;
    /** 医生姓名。 */
    private String doctorName;
    /** 就诊时间。 */
    private String clinicTime;
    /** 预约来源。 */
    private String source;
    /** 预约状态。 */
    private String status;
    /** 预约费用。 */
    private BigDecimal feeAmount;
    /** 支付时间。 */
    private LocalDateTime payTime;
    /** 签到时间。 */
    private LocalDateTime checkInTime;
    /** 取消时间。 */
    private LocalDateTime cancelTime;
    /** 取消原因。 */
    private String cancelReason;
    /** 拒诊时间。 */
    private LocalDateTime rejectTime;
    /** 拒诊原因。 */
    private String rejectReason;
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
