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
 * <p>鉴权统一由网关负责，业务模块仅在请求处理期间组装租户和用户上下文。</p>
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
     * 解析请求中的 JWT、租户头或直连用户头，并在过滤链执行期间设置上下文。
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
        TokenPrincipal principal = resolvePrincipal(request, tenantHeader, token);
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
     * @param request 当前 HTTP 请求
     * @param tenantHeader 网关透传的可信租户请求头
     * @param token 已剥离前缀的 JWT 令牌
     * @return 登录主体
     */
    private TokenPrincipal resolvePrincipal(HttpServletRequest request, String tenantHeader, String token) {
        TokenPrincipal tokenPrincipal = JwtPrincipalResolver.resolveNullable(token, jwtSecret);
        Long tokenTenantId = tokenPrincipal == null ? null : tokenPrincipal.getTenantId();
        Long headerTenantId = resolveTenantHeader(tenantHeader);
        TokenPrincipal principal = new TokenPrincipal();
        principal.setTenantId(resolveEffectiveTenantId(headerTenantId, tokenTenantId));
        fillUserPrincipal(request, principal, tokenPrincipal, headerTenantId, tokenTenantId);
        principal.setPlatformRequest(CommonConstants.isPlatformTenant(principal.getTenantId()));
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
            long tenantId = Long.parseLong(tenantHeader.trim());
            return tenantId >= CommonConstants.PLATFORM_TENANT_ID ? tenantId : CommonConstants.ISOLATED_TENANT_ID;
        } catch (NumberFormatException exception) {
            log.warn("解析租户请求头失败，tenantHeader={}", tenantHeader);
            return CommonConstants.ISOLATED_TENANT_ID;
        }
    }

    /**
     * 解析最终生效租户编号，子模块直连时以请求头租户为准。
     *
     * @param headerTenantId 请求头租户编号
     * @param tokenTenantId 令牌租户编号
     * @return 生效租户编号
     */
    private Long resolveEffectiveTenantId(Long headerTenantId, Long tokenTenantId) {
        if (isTenantMismatch(headerTenantId, tokenTenantId)) {
            log.warn("租户请求头与 JWT 租户不一致，按请求头租户构建上下文，headerTenantId={}，tokenTenantId={}",
                    headerTenantId, tokenTenantId);
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

    /**
     * 填充当前请求用户上下文。
     * <p>有效 JWT 用户声明优先；没有可用 JWT 用户声明时，才使用直连用户头。</p>
     *
     * @param request 当前 HTTP 请求
     * @param principal 当前请求登录主体
     * @param tokenPrincipal JWT 登录主体
     * @param headerTenantId 请求头租户编号
     * @param tokenTenantId 令牌租户编号
     */
    private void fillUserPrincipal(
            HttpServletRequest request,
            TokenPrincipal principal,
            TokenPrincipal tokenPrincipal,
            Long headerTenantId,
            Long tokenTenantId
    ) {
        Long headerUserId = resolveLongHeader(request, authTokenProperties.getUserHeaderName());
        String headerBusinessUserId = resolveStringHeader(request, authTokenProperties.getBusinessUserHeaderName());
        String headerUserType = resolveStringHeader(request, authTokenProperties.getUserTypeHeaderName());

        boolean tenantMatched = !isTenantMismatch(headerTenantId, tokenTenantId);
        Long tokenUserId = resolveTokenUserId(tokenPrincipal, tenantMatched);
        String tokenBusinessUserId = resolveTokenBusinessUserId(tokenPrincipal, tenantMatched);
        String tokenUserType = resolveTokenUserType(tokenPrincipal, tenantMatched);
        principal.setUserId(tokenUserId != null ? tokenUserId : headerUserId);
        principal.setBusinessUserId(StringUtils.hasText(tokenBusinessUserId) ? tokenBusinessUserId : headerBusinessUserId);
        principal.setUserType(StringUtils.hasText(tokenUserType) ? tokenUserType : headerUserType);
    }

    /**
     * 解析 Long 类型请求头。
     *
     * @param request 当前 HTTP 请求
     * @param headerName 请求头名称
     * @return Long 类型请求头值，缺失或格式错误时返回 null
     */
    private Long resolveLongHeader(HttpServletRequest request, String headerName) {
        String headerValue = resolveStringHeader(request, headerName);
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }
        try {
            return Long.parseLong(headerValue);
        } catch (NumberFormatException exception) {
            log.warn("解析 Long 类型请求头失败，headerName={}，headerValue={}", headerName, headerValue);
            return null;
        }
    }

    /**
     * 解析字符串请求头。
     *
     * @param request 当前 HTTP 请求
     * @param headerName 请求头名称
     * @return 字符串请求头值，缺失时返回 null
     */
    private String resolveStringHeader(HttpServletRequest request, String headerName) {
        if (!StringUtils.hasText(headerName)) {
            return null;
        }
        String headerValue = request.getHeader(headerName);
        return StringUtils.hasText(headerValue) ? headerValue.trim() : null;
    }

    /**
     * 解析 JWT 中的系统用户编号。
     *
     * @param tokenPrincipal JWT 登录主体
     * @param tenantMatched 租户是否匹配
     * @return 系统用户编号
     */
    private Long resolveTokenUserId(TokenPrincipal tokenPrincipal, boolean tenantMatched) {
        return tokenPrincipal != null && tenantMatched ? tokenPrincipal.getUserId() : null;
    }

    /**
     * 解析 JWT 中的业务用户编号。
     *
     * @param tokenPrincipal JWT 登录主体
     * @param tenantMatched 租户是否匹配
     * @return 业务用户编号
     */
    private String resolveTokenBusinessUserId(TokenPrincipal tokenPrincipal, boolean tenantMatched) {
        return tokenPrincipal != null && tenantMatched ? tokenPrincipal.getBusinessUserId() : null;
    }

    /**
     * 解析 JWT 中的用户类型。
     *
     * @param tokenPrincipal JWT 登录主体
     * @param tenantMatched 租户是否匹配
     * @return 用户类型
     */
    private String resolveTokenUserType(TokenPrincipal tokenPrincipal, boolean tenantMatched) {
        return tokenPrincipal != null && tenantMatched ? tokenPrincipal.getUserType() : null;
    }
}
