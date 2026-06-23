package com.hlw.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 患者档案持久化对象。
 */
@Getter
@Setter
@TableName("pat_patient")
public class PatPatientEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 关联用户编号（关联 sys_user.user_id 字符串）。 */
    private String userId;
    /** 兼容旧表患者姓名。 */
    private String name;
    /** 患者姓名。 */
    private String patientName;
    /** 患者性别。 */
    private String gender;
    /** 患者年龄。 */
    private Integer age;
    /** 联系电话。 */
    private String phone;
    /** 风险等级。 */
    private String riskLevel;
    /** 身份证号。 */
    private String idCard;
    /** 出生日期。 */
    private LocalDate birthday;
    /** 联系地址。 */
    private String address;
    /** 最近就诊日期。 */
    private LocalDate lastVisit;
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
