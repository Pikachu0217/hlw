package com.hlw.auth.service;

import jakarta.validation.constraints.NotBlank;

/**
 * 登录命令，承载账号密码认证参数。
 *
 * @param username 登录账号
 * @param password 登录密码
 */
public record LoginCommand(@NotBlank(message = "用户名不能为空") String username,
                           @NotBlank(message = "密码不能为空") String password) {
}
