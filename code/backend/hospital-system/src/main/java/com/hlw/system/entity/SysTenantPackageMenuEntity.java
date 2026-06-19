package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 租户套餐菜单关系持久化对象。
 */
@Getter
@Setter
@TableName("sys_tenant_package_menu")
public class SysTenantPackageMenuEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户套餐编号。 */
    private Long packageId;
    /** 菜单编号。 */
    private Long menuId;
}
