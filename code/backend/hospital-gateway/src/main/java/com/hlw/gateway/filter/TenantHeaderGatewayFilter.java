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

    /**
     * 根据令牌解析需要透传的租户请求头。
     *
     * @param token 登录令牌
     * @return 租户请求头值
     */
    public String resolveTenantHeader(String token) {
        Long tenantId = tokenTenantResolver.resolveTenantId(token);
        return tenantId == null ? null : String.valueOf(tenantId);
    }

    @Override
    /**
     * 过滤网关请求并透传租户编号。
     *
     * @param exchange 服务交换对象
     * @param chain 过滤链
     * @return 过滤结果
     */
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
