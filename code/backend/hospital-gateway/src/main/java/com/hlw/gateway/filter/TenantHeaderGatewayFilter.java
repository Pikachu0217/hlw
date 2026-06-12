package com.hlw.gateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

public class TenantHeaderGatewayFilter implements GlobalFilter {
    private final TokenTenantResolver tokenTenantResolver;

    public TenantHeaderGatewayFilter(TokenTenantResolver tokenTenantResolver) {
        this.tokenTenantResolver = tokenTenantResolver;
    }

    public String resolveTenantHeader(String token) {
        Long tenantId = tokenTenantResolver.resolveTenantId(token);
        return tenantId == null ? null : String.valueOf(tenantId);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("satoken");
        String tenantHeader = resolveTenantHeader(token);
        if (tenantHeader == null) {
            return chain.filter(exchange);
        }
        ServerHttpRequest mutated = exchange.getRequest().mutate()
            .header("X-Tenant-Id", tenantHeader)
            .build();
        return chain.filter(exchange.mutate().request(mutated).build());
    }
}
