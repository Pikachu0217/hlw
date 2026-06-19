package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色菜单授权展示对象。
 */
@Getter
@Setter
public class RoleMenuResp {
    /** 主键编号。 */
    private Long id;
    /** 角色编号。 */
    private Long roleId;
    /** 角色名称。 */
    private String roleName;
    /** 菜单编号。 */
    private Long menuId;
    /** 菜单名称。 */
    private String menuName;
    /** 权限标识。 */
    private String perms;
}
