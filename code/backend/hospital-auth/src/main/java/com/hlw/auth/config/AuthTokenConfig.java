package com.hlw.auth.config;

import com.hlw.auth.domain.dto.TokenIssuer;
import com.hlw.common.core.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 认证令牌配置。
 */
@Configuration
public class AuthTokenConfig {
    /**
     * 创建 JWT 令牌签发器。
     *
     * @param jwtSecret JWT 签名密钥
     * @return 令牌签发器
     */
    @Bean
    public TokenIssuer tokenIssuer(@Value("${hlw.jwt.secret}") String jwtSecret) {
        return user -> JwtUtil.issue(user.id(), user.tenantId(), user.userType(), jwtSecret);
    }
}
