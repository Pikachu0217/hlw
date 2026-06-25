package com.hlw.appointment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 放号配置持久化对象。
 */
@Getter
@Setter
@TableName("apt_number_source_release_config")
public class AptReleaseConfigEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 排班编号。 */
    private Long scheduleId;
    /** 放号时间。 */
    private LocalDateTime releaseTime;
    /** 放号数量。 */
    private Integer releaseCount;
    /** 配置状态。 */
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
    @TableLogic
    private Integer deleted;
}
