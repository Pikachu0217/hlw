package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 绑定角色菜单请求。
 */
@Getter
@Setter
public class BindRoleMenuReq {
    /** 角色编号。 */
    @NotNull(message = "角色编号不能为空")
    private Long roleId;
    /** 菜单编号。 */
    @NotNull(message = "菜单编号不能为空")
    private Long menuId;
}
