package com.hlw.doctor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 医生科室关系持久化对象。
 */
@Getter
@Setter
@TableName("doc_doctor_department")
public class DocDoctorDepartmentEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 医生编号。 */
    private Long doctorId;
    /** 科室编号。 */
    private Long departmentId;
    /** 是否免挂号费。 */
    private Integer isFree;
    /** 挂号费用。 */
    private BigDecimal appointmentFee;
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
