package com.hlw.auth.domain.req;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录命令，承载账号密码认证参数。
 *
 * @param tenantId 租户编号
 * @param username 登录账号
 * @param password 登录密码
 */
public record LoginReq(Long tenantId,
                       @NotBlank(message = "用户名不能为空") String username,
                       @NotBlank(message = "密码不能为空") String password) {
    /**
     * 使用指定租户编号创建新的登录命令。
     *
     * @param resolvedTenantId 已解析租户编号
     * @return 登录命令
     */
    public LoginReq withTenantId(Long resolvedTenantId) {
        return new LoginReq(resolvedTenantId, username, password);
    }
}
