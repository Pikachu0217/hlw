package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色菜单关系持久化对象。
 */
@Getter
@Setter
@TableName("sys_role_menu")
public class SysRoleMenuEntity extends BaseEntity {
    /** 角色编号。 */
    private Long roleId;
    /** 菜单编号。 */
    private Long menuId;
}
