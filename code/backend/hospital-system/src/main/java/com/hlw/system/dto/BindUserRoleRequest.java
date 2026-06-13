package com.hlw.system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 绑定用户角色请求。
 */
@Getter
@Setter
public class BindUserRoleRequest {
    /** 用户编号。 */
    @NotNull(message = "用户编号不能为空")
    private Long userId;
    /** 角色编号。 */
    @NotNull(message = "角色编号不能为空")
    private Long roleId;
}
