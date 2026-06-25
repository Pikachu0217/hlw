package com.hlw.consult.ws;

import com.hlw.common.core.config.AuthTokenProperties;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.security.AuthTokenResolver;
import com.hlw.common.core.security.JwtPrincipalResolver;
import com.hlw.common.core.security.TokenPrincipal;
import com.hlw.consult.service.ConsultParticipantType;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

/**
 * 问诊 WebSocket 握手上下文拦截器。
 */
@Component
@Slf4j
public class ConsultWebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private static final String CONSULT_ID_ATTRIBUTE = "consultId";
    private static final String SENDER_ID_ATTRIBUTE = "senderId";
    private static final String SENDER_TYPE_ATTRIBUTE = "senderType";
    /** 登录主体会话属性。 */
    public static final String PRINCIPAL_ATTRIBUTE = "principal";

    /** JWT 签名密钥。 */
    private final String jwtSecret;
    /** 公共认证令牌配置属性。 */
    private final AuthTokenProperties authTokenProperties;
    /** 问诊参与人校验服务。 */
    private final ConsultWebSocketPermissionService permissionService;

    /**
     * 构造问诊 WebSocket 握手鉴权拦截器。
     *
     * @param jwtSecret JWT 签名密钥
     * @param authTokenProperties 公共认证令牌配置属性
     * @param permissionService 问诊参与人鉴权服务
     */
    public ConsultWebSocketHandshakeInterceptor(
        @Value("${hlw.jwt.secret}") String jwtSecret,
        AuthTokenProperties authTokenProperties,
        ConsultWebSocketPermissionService permissionService
    ) {
        this.jwtSecret = jwtSecret;
        this.authTokenProperties = authTokenProperties;
        this.permissionService = permissionService;
    }

    /**
     * 握手前解析用户上下文并校验咨询参与权限。
     *
     * @param request 握手请求
     * @param response 握手响应
     * @param wsHandler WebSocket 处理器
     * @param attributes 会话属性
     * @return 是否允许握手
     */
    @Override
    public boolean beforeHandshake(
        ServerHttpRequest request,
        ServerHttpResponse response,
        WebSocketHandler wsHandler,
        Map<String, Object> attributes
    ) {
        Long consultId = resolveConsultId(request.getURI());
        TokenPrincipal principal = resolvePrincipal(request);
        ConsultWebSocketPermissionService.Participant participant;
        TokenPrincipalContext.set(principal);
        try {
            participant = permissionService.resolveParticipant(consultId, principal);
        } finally {
            TokenPrincipalContext.clear();
        }
        attributes.put(CONSULT_ID_ATTRIBUTE, consultId);
        attributes.put(SENDER_ID_ATTRIBUTE, participant.senderId());
        attributes.put(SENDER_TYPE_ATTRIBUTE, participant.senderType());
        attributes.put(PRINCIPAL_ATTRIBUTE, principal);
        log.info("问诊 WebSocket 握手通过，consultId={}，senderId={}，senderType={}", consultId, participant.senderId(), participant.senderType());
        return true;
    }

    /**
     * 握手完成后处理。
     *
     * @param request 握手请求
     * @param response 握手响应
     * @param wsHandler WebSocket 处理器
     * @param exception 异常
     */
    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.warn("问诊 WebSocket 握手异常", exception);
        }
    }

    /**
     * 解析问诊编号。
     *
     * @param uri 请求地址
     * @return 问诊编号
     */
    private Long resolveConsultId(URI uri) {
        String path = uri.getPath();
        String value = path.substring(path.lastIndexOf('/') + 1);
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException exception) {
            log.warn("问诊 WebSocket 路径问诊编号无效，path={}", path);
            throw new BizException(400, "问诊编号无效");
        }
    }

    /**
     * 解析登录主体。
     *
     * @param request 握手请求
     * @return 登录主体
     */
    private TokenPrincipal resolvePrincipal(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String token = AuthTokenResolver.resolve(headers.getFirst(authTokenProperties.getTokenName()), authTokenProperties.getTokenPrefix());
        if (token == null || token.isBlank()) {
            token = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams().getFirst("token");
        }
        TokenPrincipal tokenPrincipal = JwtPrincipalResolver.resolveNullable(token, jwtSecret);
        TokenPrincipal principal = buildPrincipal(headers, tokenPrincipal);
        if (principal.getTenantId() == null || principal.getTenantId() < 0L || principal.getBusinessUserId() == null || principal.getBusinessUserId().isBlank()) {
            log.warn("问诊 WebSocket 上下文缺失，tenantId={}，businessUserId={}", principal.getTenantId(), principal.getBusinessUserId());
            throw new BizException(403, "问诊 WebSocket 缺少有效用户上下文");
        }
        if (Boolean.TRUE.equals(principal.getPlatformRequest())) {
            throw new BizException(403, "平台租户不允许连接问诊 WebSocket");
        }
        if (principal.getUserType() == null || principal.getUserType().isBlank()) {
            principal.setUserType(ConsultParticipantType.PATIENT);
        }
        return principal;
    }

    /**
     * 构造 WebSocket 握手用户上下文。
     *
     * @param headers 握手请求头
     * @param tokenPrincipal JWT 登录主体
     * @return 用户上下文
     */
    private TokenPrincipal buildPrincipal(HttpHeaders headers, TokenPrincipal tokenPrincipal) {
        Long headerTenantId = resolveLongHeader(headers, authTokenProperties.getTenantHeaderName());
        Long tokenTenantId = tokenPrincipal == null ? null : tokenPrincipal.getTenantId();
        boolean tenantMatched = !isTenantMismatch(headerTenantId, tokenTenantId);

        TokenPrincipal principal = new TokenPrincipal();
        principal.setTenantId(headerTenantId != null ? headerTenantId : tokenTenantId);
        principal.setUserId(resolveUserId(headers, tokenPrincipal, tenantMatched));
        principal.setBusinessUserId(resolveBusinessUserId(headers, tokenPrincipal, tenantMatched));
        principal.setUserType(resolveUserType(headers, tokenPrincipal, tenantMatched));
        principal.setPlatformRequest(com.hlw.common.core.constants.CommonConstants.isPlatformTenant(principal.getTenantId()));
        if (isTenantMismatch(headerTenantId, tokenTenantId)) {
            log.warn("问诊 WebSocket 租户请求头与 JWT 租户不一致，按请求头租户构建上下文，headerTenantId={}，tokenTenantId={}",
                    headerTenantId, tokenTenantId);
        }
        return principal;
    }

    /**
     * 解析系统用户编号。
     *
     * @param headers 握手请求头
     * @param tokenPrincipal JWT 登录主体
     * @param tenantMatched 租户是否匹配
     * @return 系统用户编号
     */
    private Long resolveUserId(HttpHeaders headers, TokenPrincipal tokenPrincipal, boolean tenantMatched) {
        Long headerUserId = resolveLongHeader(headers, authTokenProperties.getUserHeaderName());
        Long tokenUserId = tokenPrincipal == null || !tenantMatched ? null : tokenPrincipal.getUserId();
        return tokenUserId != null ? tokenUserId : headerUserId;
    }

    /**
     * 解析业务用户编号。
     *
     * @param headers 握手请求头
     * @param tokenPrincipal JWT 登录主体
     * @param tenantMatched 租户是否匹配
     * @return 业务用户编号
     */
    private String resolveBusinessUserId(HttpHeaders headers, TokenPrincipal tokenPrincipal, boolean tenantMatched) {
        String headerBusinessUserId = resolveStringHeader(headers, authTokenProperties.getBusinessUserHeaderName());
        String tokenBusinessUserId = tokenPrincipal == null || !tenantMatched ? null : tokenPrincipal.getBusinessUserId();
        return tokenBusinessUserId != null && !tokenBusinessUserId.isBlank() ? tokenBusinessUserId : headerBusinessUserId;
    }

    /**
     * 解析用户类型。
     *
     * @param headers 握手请求头
     * @param tokenPrincipal JWT 登录主体
     * @param tenantMatched 租户是否匹配
     * @return 用户类型
     */
    private String resolveUserType(HttpHeaders headers, TokenPrincipal tokenPrincipal, boolean tenantMatched) {
        String headerUserType = resolveStringHeader(headers, authTokenProperties.getUserTypeHeaderName());
        String tokenUserType = tokenPrincipal == null || !tenantMatched ? null : tokenPrincipal.getUserType();
        return tokenUserType != null && !tokenUserType.isBlank() ? tokenUserType : headerUserType;
    }

    /**
     * 解析 Long 类型请求头。
     *
     * @param headers 握手请求头
     * @param headerName 请求头名称
     * @return Long 类型请求头值，缺失或格式错误时返回 null
     */
    private Long resolveLongHeader(HttpHeaders headers, String headerName) {
        String headerValue = resolveStringHeader(headers, headerName);
        if (headerValue == null) {
            return null;
        }
        try {
            return Long.parseLong(headerValue);
        } catch (NumberFormatException exception) {
            log.warn("问诊 WebSocket 请求头格式错误，headerName={}，headerValue={}", headerName, headerValue);
            return null;
        }
    }

    /**
     * 解析字符串请求头。
     *
     * @param headers 握手请求头
     * @param headerName 请求头名称
     * @return 字符串请求头值，缺失时返回 null
     */
    private String resolveStringHeader(HttpHeaders headers, String headerName) {
        String headerValue = headerName == null || headerName.isBlank() ? null : headers.getFirst(headerName);
        return headerValue == null || headerValue.isBlank() ? null : headerValue.trim();
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
