package com.hlw.gateway.filter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantHeaderGatewayFilterTest {
    @Test
    void null_token_returns_null_tenant() {
        DefaultTokenTenantResolver resolver = new DefaultTokenTenantResolver("test-secret-key-not-for-production");

        Long tenantId = resolver.resolveTenantId(null);

        assertThat(tenantId).isNull();
    }

    @Test
    void invalid_token_returns_null() {
        DefaultTokenTenantResolver resolver = new DefaultTokenTenantResolver("test-secret-key-not-for-production");

        Long tenantId = resolver.resolveTenantId("invalid-token");

        assertThat(tenantId).isNull();
    }

    @Test
    void blank_token_returns_null() {
        DefaultTokenTenantResolver resolver = new DefaultTokenTenantResolver("test-secret-key-not-for-production");

        Long tenantId = resolver.resolveTenantId("");

        assertThat(tenantId).isNull();
    }
}
