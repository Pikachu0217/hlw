package com.hlw.consult.web;

import com.hlw.common.core.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 问诊服务租户上下文过滤器。
 */
@Component
public class TenantContextFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(TenantContextFilter.class);
    private static final Long PLATFORM_TENANT_ID = 0L;
    private static final Long ISOLATED_TENANT_ID = -1L;

    /**
     * 从请求头解析租户上下文并写入线程变量。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 异常
     * @throws IOException IO 异常
     */
    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        String tenantHeader = request.getHeader("X-Tenant-Id");
        String token = request.getHeader("satoken");
        ResolvedTenantContext tenantContext = resolveTenantContext(tenantHeader, token);
        TenantContext.setTenantId(tenantContext.tenantId());
        TenantContext.setPlatformRequest(tenantContext.platformRequest());
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    /**
     * 解析当前请求租户上下文。
     *
     * @param tenantHeader 租户请求头
     * @param token satoken 令牌
     * @return 租户上下文
     */
    private ResolvedTenantContext resolveTenantContext(String tenantHeader, String token) {
        if (token != null && !token.isBlank()) {
            return resolveFromToken(token);
        }
        return resolveFromTenantHeader(tenantHeader);
    }

    /**
     * 从登录令牌解析租户上下文。
     *
     * @param token satoken 令牌
     * @return 租户上下文
     */
    private ResolvedTenantContext resolveFromToken(String token) {
        if (!token.startsWith("satoken-demo-")) {
            log.warn("satoken 令牌格式不受支持，token={}", token);
            return new ResolvedTenantContext(ISOLATED_TENANT_ID, false);
        }
        String[] parts = token.split("-");
        if (parts.length < 4) {
            log.warn("satoken 令牌格式不完整，token={}", token);
            return new ResolvedTenantContext(ISOLATED_TENANT_ID, false);
        }
        try {
            Long tenantId = Long.parseLong(parts[3]);
            return new ResolvedTenantContext(tenantId, PLATFORM_TENANT_ID.equals(tenantId));
        } catch (NumberFormatException exception) {
            log.warn("解析 satoken 中的租户编号失败，token={}", token);
            return new ResolvedTenantContext(ISOLATED_TENANT_ID, false);
        }
    }

    /**
     * 从租户请求头解析租户上下文。
     *
     * @param tenantHeader 租户请求头
     * @return 租户上下文
     */
    private ResolvedTenantContext resolveFromTenantHeader(String tenantHeader) {
        if (tenantHeader == null || tenantHeader.isBlank()) {
            return new ResolvedTenantContext(ISOLATED_TENANT_ID, false);
        }
        try {
            long tenantId = Long.parseLong(tenantHeader.trim());
            if (tenantId <= 0L) {
                log.warn("租户请求头不是有效的业务租户编号，tenantHeader={}", tenantHeader);
                return new ResolvedTenantContext(ISOLATED_TENANT_ID, false);
            }
            return new ResolvedTenantContext(tenantId, false);
        } catch (NumberFormatException exception) {
            log.warn("解析租户请求头失败，tenantHeader={}", tenantHeader);
            return new ResolvedTenantContext(ISOLATED_TENANT_ID, false);
        }
    }

    /**
     * 租户上下文解析结果。
     *
     * @param tenantId 租户编号
     * @param platformRequest 是否平台请求
     */
    private record ResolvedTenantContext(Long tenantId, boolean platformRequest) {
    }
}
