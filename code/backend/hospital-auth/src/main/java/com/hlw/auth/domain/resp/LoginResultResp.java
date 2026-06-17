package com.hlw.auth.domain.resp;

/**
 * 登录结果，返回登录令牌和账号上下文。
 *
 * @param token 登录令牌
 * @param tenantId 租户编号
 * @param userType 用户类型
 */
public record LoginResultResp(String token, Long tenantId, String userType) {
}
