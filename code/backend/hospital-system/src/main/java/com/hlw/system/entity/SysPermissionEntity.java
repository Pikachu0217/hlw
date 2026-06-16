package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统权限码持久化对象。
 */
@Getter
@Setter
@TableName("sys_permission")
public class SysPermissionEntity extends BaseEntity {
    /** 权限名称。 */
    private String permissionName;
    /** 权限编码。 */
    private String permissionCode;
    /** 资源类型。 */
    private String resourceType;
    /** 关联菜单编号。 */
    private Long menuId;
    /** 权限状态。 */
    private String status;
}
