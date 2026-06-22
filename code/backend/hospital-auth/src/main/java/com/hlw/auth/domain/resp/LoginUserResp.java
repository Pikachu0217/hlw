package com.hlw.auth.domain.resp;

/**
 * 登录用户信息，承载认证所需的账号数据。
 *
 * @param id 用户表主键
 * @param userId 用户业务编号
 * @param tenantId 租户编号
 * @param username 登录账号
 * @param realName 真实姓名
 * @param password 登录密码哈希
 * @param userType 用户类型
 */
public record LoginUserResp(Long id, String userId, Long tenantId, String username, String realName, String password, String userType) {
}
