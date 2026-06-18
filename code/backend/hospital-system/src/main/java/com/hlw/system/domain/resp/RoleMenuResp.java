package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色菜单授权展示对象。
 */
@Getter
@Setter
public class RoleMenuResp {
    /** 表格主键。 */
    private String key;
    /** 角色名称。 */
    private String roleName;
    /** 菜单名称。 */
    private String menuName;
    /** 权限标识。 */
    private String permission;
    /** 授权状态。 */
    private String status;
}
