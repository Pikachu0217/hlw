package com.hlw.gateway.filter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantHeaderGatewayFilterTest {
    @Test
    void token_tenant_is_forwarded_as_header() {
        TenantHeaderGatewayFilter filter = new TenantHeaderGatewayFilter(token -> 100L);

        String tenantHeader = filter.resolveTenantHeader("test-token-1");

        assertThat(tenantHeader).isEqualTo("100");
    }
}
