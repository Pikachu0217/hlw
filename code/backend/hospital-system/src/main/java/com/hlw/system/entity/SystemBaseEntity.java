package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统模块持久化对象通用字段基类。
 */
@Getter
@Setter
public class SystemBaseEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private String tenantId;
    /** 创建部门。 */
    private Long createDept;
    /** 创建者用户ID。 */
    private String createBy;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新者用户ID。 */
    private String updateBy;
    /** 更新时间。 */
    private LocalDateTime updateTime;
    /** 逻辑删除标识。 */
    @TableLogic
    private Integer deleted;
}
