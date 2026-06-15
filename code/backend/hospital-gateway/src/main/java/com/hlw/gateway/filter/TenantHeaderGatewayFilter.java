package com.hlw.gateway.filter;

import com.hlw.gateway.config.GatewayAuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * 租户请求头网关过滤器，负责基于登录令牌设置可信租户头。
 */
@Slf4j
public class TenantHeaderGatewayFilter implements GlobalFilter {

    private final TokenTenantResolver tokenTenantResolver;
    private final GatewayAuthProperties gatewayAuthProperties;

    /**
     * 构造租户请求头网关过滤器。
     *
     * @param tokenTenantResolver 登录令牌租户解析器
     * @param gatewayAuthProperties 网关认证配置属性
     */
    public TenantHeaderGatewayFilter(TokenTenantResolver tokenTenantResolver, GatewayAuthProperties gatewayAuthProperties) {
        this.tokenTenantResolver = tokenTenantResolver;
        this.gatewayAuthProperties = gatewayAuthProperties;
    }

    /**
     * 过滤请求并写入可信租户请求头。
     *
     * @param exchange 网关交换对象
     * @param chain 网关过滤链
     * @return 过滤处理结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String AUTHORIZATION_HEADER = gatewayAuthProperties.getTokenName();
        ServerHttpRequest request = exchange.getRequest();
        String token = request.getHeaders().getFirst(AUTHORIZATION_HEADER);
        boolean publicPath = isPublicPath(request);

        ServerHttpRequest cleaned = request.mutate()
                .headers(headers -> headers.remove("X-Tenant-Id"))
                .build();

        Long tenantId = resolveTenantId(token);
        if (tenantId != null && tenantId > 0) {
            cleaned = cleaned.mutate()
                    .header("X-Tenant-Id", String.valueOf(tenantId))
                    .build();
        } else if (publicPath) {
            String tenantHeader = resolvePublicTenantHeader(request);
            if (tenantHeader != null) {
                cleaned = cleaned.mutate()
                        .header("X-Tenant-Id", tenantHeader)
                        .build();
            }
        }

        if (!publicPath && tenantId == null) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange.mutate().request(cleaned).build());
    }

    /**
     * 从登录令牌解析租户编号。
     *
     * @param token 登录令牌
     * @return 租户编号，无法解析时返回 null
     */
    private Long resolveTenantId(String token) {
        return tokenTenantResolver.resolveTenantId(token);
    }

    /**
     * 判断是否公开接口。
     *
     * @param request 当前请求
     * @return 是否公开接口
     */
    private boolean isPublicPath(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        LinkedHashSet<String> publicPaths = new LinkedHashSet<>(gatewayAuthProperties.getPublicPaths() == null ? List.of() : gatewayAuthProperties.getPublicPaths());
        return publicPaths.stream()
                .filter(publicPath -> publicPath != null && !publicPath.isBlank())
                .map(String::trim)
                .anyMatch(path::startsWith);
    }

    /**
     * 解析公开接口允许透传的正数租户请求头。
     *
     * @param request 当前请求
     * @return 正数租户请求头，非法时返回 null
     */
    private String resolvePublicTenantHeader(ServerHttpRequest request) {
        String tenantHeader = request.getHeaders().getFirst("X-Tenant-Id");
        if (!StringUtils.hasText(tenantHeader)) {
            return null;
        }
        try {
            long tenantId = Long.parseLong(tenantHeader.trim());
            return tenantId > 0 ? String.valueOf(tenantId) : null;
        } catch (NumberFormatException e) {
            log.error("Invalid X-Tenant-Id header: {}", tenantHeader, e);
            return null;
        }
    }
}
