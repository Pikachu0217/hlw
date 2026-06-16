package com.hlw.common.security;

import com.hlw.common.core.config.AuthTokenProperties;
import com.hlw.common.core.security.AuthTokenResolver;
import com.hlw.common.core.tenant.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 租户上下文过滤器，从 JWT 令牌或请求头解析租户上下文并写入线程变量。
 * 所有业务模块共用此过滤器。
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Slf4j
public class JwtTenantContextFilter extends OncePerRequestFilter {
    /** 隔离租户编号，用于无法解析租户或匿名访问的场景。 */
    private static final Long ISOLATED_TENANT_ID = -1L;
    /** 平台租户编号，只有平台账号的租户编号为 0，享有跨租户管理权限。 */
    private static final Long PLATFORM_TENANT_ID = 0L;

    private final String jwtSecret;
    private final AuthTokenProperties authTokenProperties;

    /**
     * 构造 JWT 租户上下文过滤器。
     *
     * @param jwtSecret JWT 签名密钥
     * @param authTokenProperties 公共认证令牌配置属性
     */
    public JwtTenantContextFilter(
            @Value("${hlw.jwt.secret}") String jwtSecret,
            AuthTokenProperties authTokenProperties
    ) {
        this.jwtSecret = jwtSecret;
        this.authTokenProperties = authTokenProperties;
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
        String tenantHeader = request.getHeader(authTokenProperties.getTenantHeaderName());
        String token = AuthTokenResolver.resolve(
                request.getHeader(authTokenProperties.getTokenName()),
                authTokenProperties.getTokenPrefix()
        );

        Long tenantId;
        boolean platformRequest = false;

        if (tenantHeader != null && !tenantHeader.isBlank()) {
            try {
                long parsed = Long.parseLong(tenantHeader.trim());
                tenantId = parsed >= 0 ? parsed : ISOLATED_TENANT_ID;
                platformRequest = PLATFORM_TENANT_ID.equals(tenantId);
            } catch (NumberFormatException e) {
                log.warn("解析租户请求头失败，tenantHeader={}", tenantHeader);
                tenantId = ISOLATED_TENANT_ID;
            }
        } else if (token != null && !token.isBlank()) {
            tenantId = TenantJwtParser.resolveTenantId(token, jwtSecret);
            platformRequest = PLATFORM_TENANT_ID.equals(tenantId);
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
