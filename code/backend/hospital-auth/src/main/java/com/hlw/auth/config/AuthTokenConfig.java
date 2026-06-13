package com.hlw.auth.config;

import com.hlw.auth.service.TokenIssuer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 认证令牌配置。
 */
@Configuration
public class AuthTokenConfig {
    /**
     * 创建本地演示令牌签发器。
     *
     * @return 令牌签发器
     */
    @Bean
    public TokenIssuer tokenIssuer() {
        return user -> "satoken-demo-" + user.id() + "-" + user.tenantId();
    }
}
