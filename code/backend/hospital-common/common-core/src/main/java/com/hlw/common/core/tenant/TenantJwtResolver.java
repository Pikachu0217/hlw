package com.hlw.common.core.tenant;

import com.hlw.common.core.constants.CommonConstants;
import com.hlw.common.core.security.JwtPrincipalResolver;
import io.jsonwebtoken.Claims;
import com.hlw.common.core.util.JwtUtil;

/**
 * 租户 JWT 解析工具，负责从登录令牌中提取租户编号。
 */
public final class TenantJwtResolver {
    private TenantJwtResolver() {
    }

    /**
     * 从 JWT 令牌解析租户编号。
     *
     * @param token JWT 令牌
     * @param jwtSecret JWT 签名密钥
     * @return 租户编号，无法解析时返回 null
     */
    public static Long resolveTenantId(String token, String jwtSecret) {
        return JwtPrincipalResolver.resolveLongClaim(token, jwtSecret, CommonConstants.JWT_TENANT_ID);
    }

    /**
     * 从 JWT 令牌解析 Long 类型声明。
     *
     * @param token JWT 令牌
     * @param jwtSecret JWT 签名密钥
     * @param item jwt 中存储的内容 如 tenantId userId userType
     * @return Long 类型声明值，无法解析时返回 null
     */
    public static Long resolve(String token, String jwtSecret, String item) {
        return JwtPrincipalResolver.resolveLongClaim(token, jwtSecret, item);
    }

    /**
     * 解析 JWT 令牌中的 Claims。
     *
     * @param token JWT 令牌
     * @param jwtSecret JWT 签名密钥
     * @return Claims 对象
     * @throws JwtException 令牌无效或过期时抛出
     */
    public static Claims parseClaims(String token, String jwtSecret) {
        return JwtUtil.parseClaims(token, jwtSecret);
    }
}
