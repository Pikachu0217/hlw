package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * 租户信息持久化对象。
 */
@Getter
@Setter
@TableName("sys_tenant")
public class SysTenantEntity extends BaseEntity {
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
}
