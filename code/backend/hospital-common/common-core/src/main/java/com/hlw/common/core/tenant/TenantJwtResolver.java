package com.hlw.common.core.tenant;

import com.hlw.common.core.constants.CommonConstants;
import com.hlw.common.core.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.StringUtils;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

import lombok.extern.slf4j.Slf4j;

/**
 * 租户 JWT 解析工具，负责从登录令牌中提取租户编号。
 */
@Slf4j
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
        return resolve(token, jwtSecret, CommonConstants.JWT_TENANT_ID);
    }

    /**
     * 从 JWT 令牌解析租户编号。
     *
     * @param token JWT 令牌
     * @param jwtSecret JWT 签名密钥
     * @param item jwt 中存储的内容 如 tenantId userId userType
     * @return 租户编号，无法解析时返回 null
     */
    public static Long resolve(String token, String jwtSecret, String item) {
        if (!StringUtils.hasText(token)) {
            return null;
        }
        try {
            Claims claims = JwtUtil.parseClaims(token, jwtSecret);
            Object tenantId = claims.get(CommonConstants.JWT_TENANT_ID);
            return tenantId instanceof Number ? ((Number) tenantId).longValue() : null;
        } catch (JwtException e) {
            log.error("解析 JWT 令牌失败", e);
            return null;
        }
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
     * @param jwtSecret JWT 签名密钥
     * @return 签名 Key
     */
    private static Key keyFrom(String jwtSecret) {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, SignatureAlgorithm.HS256.getJcaName());
    }
}
