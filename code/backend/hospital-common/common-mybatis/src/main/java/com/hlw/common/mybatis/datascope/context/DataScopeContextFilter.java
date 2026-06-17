package com.hlw.common.mybatis.datascope.context;

import com.hlw.common.core.tenant.TokenPrincipalContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 请求级数据权限上下文过滤器。
 *
 * <p>在认证/租户过滤器写入 {@link TokenPrincipalContext} 后调用业务提供的
 * {@link DataScopeLoader}，并保证请求结束后清理 {@link DataScopeContextHolder}。</p>
 */
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 20)
@ConditionalOnBean(DataScopeLoader.class)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class DataScopeContextFilter extends OncePerRequestFilter {

    private final DataScopeLoader dataScopeLoader;

    public DataScopeContextFilter(DataScopeLoader dataScopeLoader) {
        this.dataScopeLoader = dataScopeLoader;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        var principal = TokenPrincipalContext.get();
        DataScopeContext context;
        if (principal == null) {
            context = null;
        } else if (Boolean.TRUE.equals(principal.getPlatformRequest())) {
            context = DataScopeContext.builder().ignoreAll(true).build();
        } else {
            context = dataScopeLoader.load(principal.getUserId(), principal.getTenantId());
        }
        if (context != null) {
            DataScopeContextHolder.set(context);
        }
        try {
            filterChain.doFilter(request, response);
        } finally {
            DataScopeContextHolder.clear();
        }
    }
}
