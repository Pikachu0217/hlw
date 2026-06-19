package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 绑定用户角色请求。
 */
@Getter
@Setter
public class BindUserRoleReq {
    /** 用户编号。 */
    @NotBlank(message = "用户编号不能为空")
    private String userId;
    /** 角色编号。 */
    @NotNull(message = "角色编号不能为空")
    private Long roleId;
}
