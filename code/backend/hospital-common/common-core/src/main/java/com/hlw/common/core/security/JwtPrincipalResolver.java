package com.hlw.common.core.security;

import com.hlw.common.core.constants.CommonConstants;
import com.hlw.common.core.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * JWT 登录主体解析器，统一从登录令牌中解析用户、租户和用户类型。
 */
@Slf4j
public final class JwtPrincipalResolver {

    /**
     * 私有构造方法，防止实例化工具类。
     */
    private JwtPrincipalResolver() {
    }

    /**
     * 从 JWT 令牌解析登录主体。
     *
     * @param token JWT 令牌
     * @param jwtSecret JWT 签名密钥
     * @return 登录主体，令牌缺失、无效或过期时返回 null
     */
    public static TokenPrincipal resolveNullable(String token, String jwtSecret) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            return fromClaims(JwtUtil.parseClaims(token, jwtSecret));
        } catch (JwtException exception) {
            log.warn("解析 JWT 登录主体失败，message={}", exception.getMessage());
            return null;
        }
    }

    /**
     * 从 JWT 令牌解析 Long 类型声明。
     *
     * @param token JWT 令牌
     * @param jwtSecret JWT 签名密钥
     * @param claimName 声明名称
     * @return Long 类型声明值，无法解析时返回 null
     */
    public static Long resolveLongClaim(String token, String jwtSecret, String claimName) {
        TokenPrincipal principal = resolveNullable(token, jwtSecret);
        if (CommonConstants.JWT_TENANT_ID.equals(claimName)) {
            return principal == null ? null : principal.getTenantId();
        }
        if (CommonConstants.JWT_USER_ID.equals(claimName)) {
            return principal == null ? null : principal.getUserId();
        }
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            Object value = JwtUtil.parseClaims(token, jwtSecret).get(claimName);
            return value instanceof Number ? ((Number) value).longValue() : null;
        } catch (JwtException exception) {
            log.warn("解析 JWT Long 声明失败，claimName={}，message={}", claimName, exception.getMessage());
            return null;
        }
    }

    /**
     * 从 JWT Claims 转换登录主体。
     *
     * @param claims JWT Claims
     * @return 登录主体
     */
    private static TokenPrincipal fromClaims(Claims claims) {
        TokenPrincipal principal = new TokenPrincipal();
        principal.setUserId(resolveLong(claims, CommonConstants.JWT_USER_ID));
        principal.setTenantId(resolveLong(claims, CommonConstants.JWT_TENANT_ID));
        principal.setUserType(resolveString(claims, CommonConstants.JWT_USER_TYPE));
        principal.setPlatformRequest(CommonConstants.isPlatformTenant(principal.getTenantId()));
        return principal;
    }

    /**
     * 从 Claims 中读取 Long 类型声明。
     *
     * @param claims JWT Claims
     * @param claimName 声明名称
     * @return Long 类型声明值
     */
    private static Long resolveLong(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        return value instanceof Number ? ((Number) value).longValue() : null;
    }

    /**
     * 从 Claims 中读取 String 类型声明。
     *
     * @param claims JWT Claims
     * @param claimName 声明名称
     * @return String 类型声明值
     */
    private static String resolveString(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        return value == null ? null : String.valueOf(value);
    }
}
