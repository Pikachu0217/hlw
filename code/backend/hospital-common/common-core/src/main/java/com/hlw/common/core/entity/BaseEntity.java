package com.hlw.common.core.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 持久化对象通用字段基类。
 * <p>
 * 集中维护所有业务实体共用的主键、租户、审计与逻辑删除字段，
 * 避免在各模块 {@code *Entity} 中重复声明。
 * </p>
 */
@Getter
@Setter
public class BaseEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
    /** 创建人编号。 */
    private Long createBy;
    /** 更新人编号。 */
    private Long updateBy;
    /** 租户编号。 */
    private Long tenantId;
    /** 逻辑删除标识。 */
    @TableLogic
    private Integer deleted;
}
