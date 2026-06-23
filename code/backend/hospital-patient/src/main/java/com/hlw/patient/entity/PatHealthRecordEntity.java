package com.hlw.patient.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 健康档案持久化对象。
 */
@Getter
@Setter
@TableName("pat_health_record")
public class PatHealthRecordEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 患者档案编号（关联 pat_patient.id）。 */
    private Long patientId;
    /** 档案标题。 */
    private String title;
    /** 档案摘要。 */
    private String summary;
    /** 过敏史。 */
    private String allergies;
    /** 既往病史。 */
    private String history;
    /** 诊断信息。 */
    private String diagnosis;
    /** 备注。 */
    private String remark;
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
