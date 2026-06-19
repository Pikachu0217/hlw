package com.hlw.common.security;

import com.hlw.common.core.config.AuthTokenProperties;
import com.hlw.common.core.constants.CommonConstants;
import com.hlw.common.core.security.AuthTokenResolver;
import com.hlw.common.core.security.JwtPrincipalResolver;
import com.hlw.common.core.security.TokenPrincipal;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
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

    private final String jwtSecret;
    private final AuthTokenProperties authTokenProperties;

    /**
     * 构造 JWT 租户上下文过滤器。
     *
     * @param jwtSecret           JWT 签名密钥
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
     * @param request     当前 HTTP 请求
     * @param response    当前 HTTP 响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet 处理异常
     * @throws IOException      IO 处理异常
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
        TokenPrincipal principal = resolvePrincipal(tenantHeader, token);
        TokenPrincipalContext.set(principal);
        try {
            filterChain.doFilter(request, response);
        } finally {
            TokenPrincipalContext.clear();
        }
    }

    /**
     * 解析当前请求登录主体。
     *
     * @param tenantHeader 网关透传的可信租户请求头
     * @param token 已剥离前缀的 JWT 令牌
     * @return 登录主体
     */
    private TokenPrincipal resolvePrincipal(String tenantHeader, String token) {
        TokenPrincipal tokenPrincipal = JwtPrincipalResolver.resolveNullable(token, jwtSecret);
        Long tokenTenantId = tokenPrincipal == null ? null : tokenPrincipal.getTenantId();
        Long headerTenantId = resolveTenantHeader(tenantHeader);
        TokenPrincipal principal = new TokenPrincipal();
        principal.setTenantId(resolveEffectiveTenantId(headerTenantId, tokenTenantId));
        if (!isTenantMismatch(headerTenantId, tokenTenantId)) {
            principal.setUserId(tokenPrincipal == null ? null : tokenPrincipal.getUserId());
            principal.setUserType(tokenPrincipal == null ? null : tokenPrincipal.getUserType());
        }
        principal.setPlatformRequest(CommonConstants.PLATFORM_TENANT_ID.equals(principal.getTenantId()));
        return principal;
    }

    /**
     * 解析可信租户请求头。
     *
     * @param tenantHeader 租户请求头
     * @return 租户编号，缺失时返回 null，格式错误时返回隔离租户编号
     */
    private Long resolveTenantHeader(String tenantHeader) {
        if (!StringUtils.hasText(tenantHeader)) {
            return null;
        }
        try {
            return Long.parseLong(tenantHeader.trim());
        } catch (NumberFormatException exception) {
            log.warn("解析租户请求头失败，tenantHeader={}", tenantHeader);
            return CommonConstants.ISOLATED_TENANT_ID;
        }
    }

    /**
     * 解析最终生效租户编号。
     *
     * @param headerTenantId 请求头租户编号
     * @param tokenTenantId 令牌租户编号
     * @return 生效租户编号
     */
    private Long resolveEffectiveTenantId(Long headerTenantId, Long tokenTenantId) {
        if (isTenantMismatch(headerTenantId, tokenTenantId)) {
            log.warn("租户请求头与 JWT 租户不一致，headerTenantId={}，tokenTenantId={}", headerTenantId, tokenTenantId);
            return CommonConstants.ISOLATED_TENANT_ID;
        }
        if (headerTenantId != null) {
            return headerTenantId;
        }
        if (tokenTenantId != null) {
            return tokenTenantId;
        }
        return CommonConstants.ISOLATED_TENANT_ID;
    }

    /**
     * 判断请求头租户和令牌租户是否冲突。
     *
     * @param headerTenantId 请求头租户编号
     * @param tokenTenantId 令牌租户编号
     * @return 是否冲突
     */
    private boolean isTenantMismatch(Long headerTenantId, Long tokenTenantId) {
        return headerTenantId != null
                && tokenTenantId != null
                && !headerTenantId.equals(tokenTenantId);
    }
}
