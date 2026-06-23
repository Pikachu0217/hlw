package com.hlw.doctor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生持久化对象。
 */
@Getter
@Setter
@TableName("doc_doctor")
public class DocDoctorEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 关联用户编号（关联 sys_user.id）。 */
    private Long userId;
    /** 租户编号。 */
    private Long tenantId;
    /** 兼容旧表医生姓名。 */
    private String name;
    /** 医生姓名。 */
    private String doctorName;
    /** 头像地址。 */
    private String avatar;
    /** 医生职称。 */
    private String title;
    /** 所属科室。 */
    private String department;
    /** 擅长方向。 */
    private String specialty;
    /** 医生简介。 */
    private String introduction;
    /** 问诊费用。 */
    private BigDecimal consultFee;
    /** 接诊状态。 */
    private String consultStatus;
    /** 评分。 */
    private BigDecimal rating;
    /** 展示状态。 */
    private String status;
    /** 排班描述。 */
    private String scheduleDesc;
    /** 当前接诊患者数。 */
    private Integer patientCount;
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
