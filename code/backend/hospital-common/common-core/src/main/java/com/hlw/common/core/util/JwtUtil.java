package com.hlw.common.core.util;

import com.hlw.common.core.constants.CommonConstants;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 令牌工具，负责签发和校验 JWT 令牌。
 */
public final class JwtUtil {

    private JwtUtil() {
    }

    /**
     * 签发 JWT 令牌。
     *
     * @param userId   用户编号
     * @param tenantId 租户编号
     * @param userType 用户类型
     * @param secret   HMAC 密钥
     * @return JWT 令牌字符串
     */
    public static String issue(Long userId, Long tenantId, String userType, String secret) {
        Key key = keyFrom(secret);
        Date now = new Date();
        Map<String, Object> claims = new HashMap<>();
        claims.put(CommonConstants.JWT_USER_ID, userId);
        claims.put(CommonConstants.JWT_TENANT_ID, tenantId);
        claims.put(CommonConstants.JWT_USER_TYPE, userType);
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + CommonConstants.JWT_DEFAULT_EXPIRATION_MS))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
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
        return Jwts.parserBuilder()
                .setSigningKey(keyFrom(jwtSecret))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 根据配置密钥创建 HMAC 签名 Key。
     *
     * @param secret HMAC 密钥
     * @return 签名 Key
     */
    private static Key keyFrom(String secret) {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }
}
