package com.hlw.common.security;

import com.hlw.common.core.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 租户上下文过滤器，从 JWT 令牌或请求头解析租户上下文并写入线程变量。
 * 所有业务模块共用此过滤器。
 */
@Component
public class JwtTenantContextFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtTenantContextFilter.class);
    private static final Long ISOLATED_TENANT_ID = -1L;

    private final String jwtSecret;

    /**
     * 构造 JWT 租户上下文过滤器。
     *
     * @param jwtSecret JWT 签名密钥
     */
    public JwtTenantContextFilter(@Value("${hlw.jwt.secret}") String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    /**
     * 解析请求中的 JWT 或租户头，并在过滤链执行期间设置租户上下文。
     *
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 处理异常
     * @throws IOException IO 处理异常
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String tenantHeader = request.getHeader("X-Tenant-Id");
        String token = request.getHeader("satoken");

        Long tenantId;
        boolean platformRequest = false;

        if (token != null && !token.isBlank()) {
            tenantId = TenantJwtParser.resolveTenantId(token, jwtSecret);
            platformRequest = tenantId != null && tenantId == 0L;
        } else if (tenantHeader != null && !tenantHeader.isBlank()) {
            try {
                long parsed = Long.parseLong(tenantHeader.trim());
                tenantId = parsed > 0 ? parsed : ISOLATED_TENANT_ID;
                platformRequest = tenantId == 0L;
            } catch (NumberFormatException e) {
                log.warn("解析租户请求头失败，tenantHeader={}", tenantHeader);
                tenantId = ISOLATED_TENANT_ID;
            }
        } else {
            tenantId = ISOLATED_TENANT_ID;
        }

        TenantContext.setTenantId(tenantId);
        TenantContext.setPlatformRequest(platformRequest);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }
}
