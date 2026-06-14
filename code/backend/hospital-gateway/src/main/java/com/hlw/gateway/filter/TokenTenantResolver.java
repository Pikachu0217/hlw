package com.hlw.gateway.filter;

/**
 * 登录令牌租户解析器，负责把令牌转换为租户编号。
 */
@FunctionalInterface
public interface TokenTenantResolver {
    /**
     * 从登录令牌解析租户编号。
     *
     * @param token 登录令牌
     * @return 租户编号，无法解析时返回 null
     */
    Long resolveTenantId(String token);
}
