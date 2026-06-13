package com.hlw.doctor.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医生排班持久化对象。
 */
@Getter
@Setter
@TableName("doc_schedule")
public class DocScheduleEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 医生编号。 */
    private Long doctorId;
    /** 出诊时段。 */
    private String slot;
    /** 排班日期。 */
    private LocalDate scheduleDate;
    /** 排班时间段。 */
    private String timeSlot;
    /** 总号源数量。 */
    private Integer totalNumber;
    /** 剩余号源数量。 */
    private Integer remainNumber;
    /** 排班状态。 */
    private String status;
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
