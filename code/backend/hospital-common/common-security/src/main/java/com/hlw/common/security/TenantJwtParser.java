package com.hlw.common.security;

import com.hlw.common.core.tenant.TenantJwtResolver;

/**
 * 租户上下文 JWT 解析器，从 JWT 令牌中提取租户编号。
 */
public final class TenantJwtParser {
    private static final Long ISOLATED_TENANT_ID = -1L;

    private TenantJwtParser() {
    }

    /**
     * 从 JWT 令牌解析租户编号。
     *
     * @param token     JWT 令牌
     * @param jwtSecret JWT 签名密钥
     * @return 租户编号，无法解析时返回 -1
     */
    public static Long resolveTenantId(String token, String jwtSecret) {
        Long tenantId = resolveNullableTenantId(token, jwtSecret);
        return tenantId == null ? ISOLATED_TENANT_ID : tenantId;
    }

    /**
     * 从 JWT 令牌解析租户编号。
     *
     * @param token     JWT 令牌
     * @param jwtSecret JWT 签名密钥
     * @return 租户编号，无法解析时返回 null
     */
    public static Long resolveNullableTenantId(String token, String jwtSecret) {
        return TenantJwtResolver.resolveTenantId(token, jwtSecret);
    }
}
