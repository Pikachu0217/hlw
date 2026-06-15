package com.hlw.auth.service;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * 登录命令，承载账号密码认证参数。
 *
 * @param tenantId 租户编号
 * @param username 登录账号
 * @param password 登录密码
 */
public record LoginCommand(@NotNull(message = "租户不能为空") @Positive(message = "租户编号必须大于0") Long tenantId,
                           @NotBlank(message = "用户名不能为空") String username,
                           @NotBlank(message = "密码不能为空") String password) {
}
