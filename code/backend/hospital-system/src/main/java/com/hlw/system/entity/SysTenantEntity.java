package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 租户信息持久化对象。
 */
@Getter
@Setter
@TableName("sys_tenant")
public class SysTenantEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 兼容旧表租户名称。 */
    private String name;
    /** 租户名称。 */
    private String tenantName;
    /** 套餐名称。 */
    private String packageName;
    /** 管理员名称。 */
    private String adminName;
    /** 到期日期。 */
    private LocalDate expireAt;
    /** 租户状态。 */
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
