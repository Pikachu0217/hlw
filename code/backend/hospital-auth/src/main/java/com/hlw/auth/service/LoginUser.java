package com.hlw.auth.service;

/**
 * 登录用户信息，承载认证所需的账号数据。
 *
 * @param id 用户编号
 * @param tenantId 租户编号
 * @param username 登录账号
 * @param password 登录密码哈希
 * @param userType 用户类型
 */
public record LoginUser(Long id, Long tenantId, String username, String password, String userType) {
}
