package com.hlw.gateway.filter;

import com.hlw.gateway.config.GatewayAuthProperties;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantHeaderGatewayFilterTest {
    @Test
    void null_token_returns_null_tenant() {
        DefaultTokenTenantResolver resolver = newResolver();

        Long tenantId = resolver.resolveTenantId(null);

        assertThat(tenantId).isNull();
    }

    @Test
    void invalid_token_returns_null() {
        DefaultTokenTenantResolver resolver = newResolver();

        Long tenantId = resolver.resolveTenantId("invalid-token");

        assertThat(tenantId).isNull();
    }

    @Test
    void blank_token_returns_null() {
        DefaultTokenTenantResolver resolver = newResolver();

        Long tenantId = resolver.resolveTenantId("");

        assertThat(tenantId).isNull();
    }

    /**
     * 创建默认登录令牌租户解析器。
     *
     * @return 默认登录令牌租户解析器
     */
    private DefaultTokenTenantResolver newResolver() {
        GatewayAuthProperties properties = new GatewayAuthProperties();
        properties.setTokenPrefix("Bearer");
        return new DefaultTokenTenantResolver("test-secret-key-not-for-production", properties.getTokenPrefix());
    }
}
