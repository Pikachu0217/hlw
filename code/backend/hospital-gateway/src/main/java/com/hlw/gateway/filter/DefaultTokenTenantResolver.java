package com.hlw.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * 默认登录令牌租户解析器，负责从 JWT 中解析租户编号。
 */
public class DefaultTokenTenantResolver implements TokenTenantResolver {
    private final Key secretKey;
    private final String tokenPrefix;

    /**
     * 构造默认登录令牌租户解析器。
     *
     * @param jwtSecret JWT 签名密钥
     * @param tokenPrefix 登录令牌前缀
     */
    public DefaultTokenTenantResolver(String jwtSecret, String tokenPrefix) {
        this.secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName()
        );
        this.tokenPrefix = tokenPrefix == null ? "" : tokenPrefix.trim();
    }

    /**
     * 从登录令牌解析租户编号。
     *
     * @param token 登录令牌
     * @return 租户编号，无法解析时返回 null
     */
    @Override
    public Long resolveTenantId(String token) {
        String rawToken = resolveRawToken(token);
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(rawToken)
                    .getBody();
            Object tenantId = claims.get("tenantId");
            return tenantId instanceof Number ? ((Number) tenantId).longValue() : null;
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * 从 Authorization 请求头中提取原始登录令牌。
     *
     * @param token Authorization 请求头值
     * @return 原始登录令牌，缺少令牌时返回 null
     */
    private String resolveRawToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        String trimmed = token.trim();
        String prefixWithSpace = tokenPrefix + " ";
        if (trimmed.regionMatches(true, 0, prefixWithSpace, 0, prefixWithSpace.length())) {
            return trimmed.substring(prefixWithSpace.length()).trim();
        }
        return trimmed;
    }
}
