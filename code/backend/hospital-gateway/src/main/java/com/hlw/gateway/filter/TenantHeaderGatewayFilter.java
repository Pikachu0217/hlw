package com.hlw.gateway.filter;

import com.hlw.common.core.config.AuthTokenProperties;
import com.hlw.gateway.config.GatewayAuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * 租户请求头网关过滤器，负责基于登录令牌设置可信租户头。
 */
@Slf4j
public class TenantHeaderGatewayFilter implements GlobalFilter {

    private final TokenTenantResolver tokenTenantResolver;
    private final GatewayAuthProperties gatewayAuthProperties;
    private final AuthTokenProperties authTokenProperties;

    /**
     * 构造租户请求头网关过滤器。
     *
     * @param tokenTenantResolver 登录令牌租户解析器
     * @param gatewayAuthProperties 网关认证配置属性
     * @param authTokenProperties 公共认证令牌配置属性
     */
    public TenantHeaderGatewayFilter(
            TokenTenantResolver tokenTenantResolver,
            GatewayAuthProperties gatewayAuthProperties,
            AuthTokenProperties authTokenProperties
    ) {
        this.tokenTenantResolver = tokenTenantResolver;
        this.gatewayAuthProperties = gatewayAuthProperties;
        this.authTokenProperties = authTokenProperties;
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
        ServerHttpRequest request = exchange.getRequest();
        String token = request.getHeaders().getFirst(authTokenProperties.getTokenName());
        boolean publicPath = isPublicPath(request);

        ServerHttpRequest cleaned = request.mutate()
                .headers(headers -> headers.remove(authTokenProperties.getTenantHeaderName()))
                .build();

        Long tenantId = resolveTenantId(token);
        if (tenantId != null && tenantId >= 0) {
            cleaned = cleaned.mutate()
                    .header(authTokenProperties.getTenantHeaderName(), String.valueOf(tenantId))
                    .build();
        } else if (publicPath) {
            String tenantHeader = resolvePublicTenantHeader(request);
            if (tenantHeader != null) {
                cleaned = cleaned.mutate()
                        .header(authTokenProperties.getTenantHeaderName(), tenantHeader)
                        .build();
            }
        }

        if (!publicPath && (tenantId == null || tenantId < 0)) {
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
     * <p>匹配规则：以 {@code /**} 结尾的配置项按前缀匹配，其余按精确路径匹配，
     * 避免类似 {@code /auth/login-xxx} 被 {@code /auth/login} 误判为公开接口。</p>
     *
     * @param request 当前请求
     * @return 是否公开接口
     */
    private boolean isPublicPath(ServerHttpRequest request) {
        String path = request.getURI().getPath();
        List<String> publicPaths = gatewayAuthProperties.getPublicPaths() == null
                ? List.of() : gatewayAuthProperties.getPublicPaths();
        return publicPaths.stream()
                .filter(publicPath -> publicPath != null && !publicPath.isBlank())
                .map(String::trim)
                .anyMatch(publicPath -> matchesPublicPath(publicPath, path));
    }

    /**
     * 判断请求路径是否命中单个公开接口配置。
     *
     * @param publicPath 公开接口配置，以 {@code /**} 结尾时按目录前缀匹配，否则精确匹配
     * @param requestPath 请求路径
     * @return 是否命中
     */
    private boolean matchesPublicPath(String publicPath, String requestPath) {
        if (publicPath.endsWith("/**")) {
            String prefix = publicPath.substring(0, publicPath.length() - 3);
            return requestPath.equals(prefix) || requestPath.startsWith(prefix + "/");
        }
        return requestPath.equals(publicPath);
    }

    /**
     * 解析公开接口允许透传的正数租户请求头。
     *
     * @param request 当前请求
     * @return 正数租户请求头，非法时返回 null
     */
    private String resolvePublicTenantHeader(ServerHttpRequest request) {
        String tenantHeader = request.getHeaders().getFirst(authTokenProperties.getTenantHeaderName());
        if (!StringUtils.hasText(tenantHeader)) {
            return null;
        }
        try {
            long tenantId = Long.parseLong(tenantHeader.trim());
            return tenantId > 0 ? String.valueOf(tenantId) : null;
        } catch (NumberFormatException e) {
            log.error("公开接口租户请求头格式错误，tenantHeaderName={}，tenantHeader={}",
                    authTokenProperties.getTenantHeaderName(), tenantHeader, e);
            return null;
        }
    }
}
