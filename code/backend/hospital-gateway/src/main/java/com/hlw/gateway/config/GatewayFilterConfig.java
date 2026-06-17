package com.hlw.gateway.config;

import com.hlw.common.core.config.AuthTokenProperties;
import com.hlw.gateway.filter.DefaultTokenTenantResolver;
import com.hlw.gateway.filter.TenantHeaderGatewayFilter;
import com.hlw.gateway.filter.TokenBlacklistChecker;
import com.hlw.gateway.filter.TokenTenantResolver;
import org.redisson.api.RedissonReactiveClient;
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
     * @param authTokenProperties 公共认证令牌配置属性
     * @return 租户请求头过滤器
     */
    @Bean
    public TenantHeaderGatewayFilter tenantHeaderGatewayFilter(
            TokenTenantResolver tokenTenantResolver,
            GatewayAuthProperties gatewayAuthProperties,
            AuthTokenProperties authTokenProperties,
            TokenBlacklistChecker tokenBlacklistChecker
    ) {
        return new TenantHeaderGatewayFilter(
                tokenTenantResolver, gatewayAuthProperties, authTokenProperties, tokenBlacklistChecker);
    }

    /**
     * 创建 JWT 令牌租户解析器。
     *
     * @param jwtSecret JWT 签名密钥
     * @param authTokenProperties 公共认证令牌配置属性
     * @return 租户解析器
     */
    @Bean
    public TokenTenantResolver tokenTenantResolver(
            @Value("${hlw.jwt.secret}") String jwtSecret,
            AuthTokenProperties authTokenProperties
    ) {
        return new DefaultTokenTenantResolver(jwtSecret, authTokenProperties.getTokenPrefix());
    }

    /**
     * 创建退出登录令牌黑名单校验器。
     *
     * @param redissonReactiveClient Redisson 反应式客户端
     * @param authTokenProperties 公共认证令牌配置属性
     * @return Token 黑名单校验器
     */
    @Bean
    public TokenBlacklistChecker tokenBlacklistChecker(
            RedissonReactiveClient redissonReactiveClient,
            AuthTokenProperties authTokenProperties
    ) {
        return new TokenBlacklistChecker(redissonReactiveClient, authTokenProperties.getTokenPrefix());
    }
}
