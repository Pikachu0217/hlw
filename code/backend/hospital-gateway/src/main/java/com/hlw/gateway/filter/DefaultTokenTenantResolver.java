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

    /**
     * 构造默认登录令牌租户解析器。
     *
     * @param jwtSecret JWT 签名密钥
     */
    public DefaultTokenTenantResolver(String jwtSecret) {
        this.secretKey = new SecretKeySpec(
                jwtSecret.getBytes(StandardCharsets.UTF_8),
                SignatureAlgorithm.HS256.getJcaName()
        );
    }

    /**
     * 从登录令牌解析租户编号。
     *
     * @param token 登录令牌
     * @return 租户编号，无法解析时返回 null
     */
    @Override
    public Long resolveTenantId(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object tenantId = claims.get("tenantId");
            return tenantId instanceof Number ? ((Number) tenantId).longValue() : null;
        } catch (JwtException e) {
            return null;
        }
    }
}
