package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 租户套餐菜单关系持久化对象。
 */
@Getter
@Setter
@TableName("sys_tenant_package_menu")
public class SysTenantPackageMenuEntity extends BaseEntity {
    /** 租户套餐编号。 */
    private Long packageId;
    /** 菜单编号。 */
    private Long menuId;
}
