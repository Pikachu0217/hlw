package com.hlw.gateway.config;

import com.hlw.gateway.filter.TenantHeaderGatewayFilter;
import com.hlw.gateway.filter.TokenTenantResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关过滤器配置。
 */
@Configuration
public class GatewayFilterConfig {
    /**
     * 创建租户请求头透传过滤器。
     *
     * @param tokenTenantResolver 租户解析器
     * @return 租户请求头过滤器
     */
    @Bean
    public TenantHeaderGatewayFilter tenantHeaderGatewayFilter(TokenTenantResolver tokenTenantResolver) {
        return new TenantHeaderGatewayFilter(tokenTenantResolver);
    }

    /**
     * 创建本地演示令牌租户解析器。
     *
     * @return 租户解析器
     */
    @Bean
    public TokenTenantResolver tokenTenantResolver() {
        return token -> token == null || token.isBlank() ? null : 100L;
    }
}
