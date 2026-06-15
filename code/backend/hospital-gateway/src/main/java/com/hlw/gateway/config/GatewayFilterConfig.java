package com.hlw.gateway.config;

import com.hlw.gateway.filter.DefaultTokenTenantResolver;
import com.hlw.gateway.filter.TenantHeaderGatewayFilter;
import com.hlw.gateway.filter.TokenTenantResolver;
import org.springframework.beans.factory.annotation.Value;
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
     * @param gatewayAuthProperties 网关认证配置属性
     * @return 租户请求头过滤器
     */
    @Bean
    public TenantHeaderGatewayFilter tenantHeaderGatewayFilter(
            TokenTenantResolver tokenTenantResolver,
            GatewayAuthProperties gatewayAuthProperties
    ) {
        return new TenantHeaderGatewayFilter(tokenTenantResolver, gatewayAuthProperties.getPublicPaths());
    }

    /**
     * 创建 JWT 令牌租户解析器。
     *
     * @param jwtSecret JWT 签名密钥
     * @param gatewayAuthProperties 网关认证配置属性
     * @return 租户解析器
     */
    @Bean
    public TokenTenantResolver tokenTenantResolver(
            @Value("${hlw.jwt.secret}") String jwtSecret,
            GatewayAuthProperties gatewayAuthProperties
    ) {
        return new DefaultTokenTenantResolver(jwtSecret, gatewayAuthProperties.getTokenPrefix());
    }
}
