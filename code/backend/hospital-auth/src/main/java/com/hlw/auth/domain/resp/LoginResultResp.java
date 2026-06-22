package com.hlw.auth.domain.resp;

/**
 * 登录结果，返回登录令牌和账号上下文。
 *
 * @param token 登录令牌
 * @param tenantId 租户编号
 * @param username 登录账号
 * @param realName 真实姓名
 * @param userType 用户类型
 */
public record LoginResultResp(String token, Long tenantId, String username, String realName, String userType) {
}
