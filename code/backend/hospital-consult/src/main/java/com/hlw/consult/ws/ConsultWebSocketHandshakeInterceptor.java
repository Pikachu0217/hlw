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
 * 问诊 WebSocket 握手鉴权拦截器。
 */
@Component
@Slf4j
public class ConsultWebSocketHandshakeInterceptor implements HandshakeInterceptor {
    private static final String CONSULT_ID_ATTRIBUTE = "consultId";
    private static final String SENDER_ID_ATTRIBUTE = "senderId";
    private static final String SENDER_TYPE_ATTRIBUTE = "senderType";

    /** JWT 签名密钥。 */
    private final String jwtSecret;
    /** 公共认证令牌配置属性。 */
    private final AuthTokenProperties authTokenProperties;
    /** 问诊参与人鉴权服务。 */
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
     * 握手前解析登录用户并校验咨询参与权限。
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
        String senderType;
        TokenPrincipalContext.set(principal);
        try {
            senderType = permissionService.resolveSenderType(consultId, principal);
        } finally {
            TokenPrincipalContext.clear();
        }
        attributes.put(CONSULT_ID_ATTRIBUTE, consultId);
        attributes.put(SENDER_ID_ATTRIBUTE, principal.getBusinessUserId());
        attributes.put(SENDER_TYPE_ATTRIBUTE, senderType);
        log.info("问诊 WebSocket 握手通过，consultId={}，senderId={}，senderType={}", consultId, principal.getBusinessUserId(), senderType);
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
        TokenPrincipal principal = JwtPrincipalResolver.resolveNullable(token, jwtSecret);
        if (principal == null || principal.getTenantId() == null || principal.getBusinessUserId() == null || principal.getBusinessUserId().isBlank()) {
            log.warn("问诊 WebSocket 鉴权失败，令牌无效");
            throw new BizException(401, "登录令牌无效");
        }
        if (Boolean.TRUE.equals(principal.getPlatformRequest())) {
            throw new BizException(403, "平台租户不允许连接问诊 WebSocket");
        }
        if (principal.getUserType() == null || principal.getUserType().isBlank()) {
            principal.setUserType(ConsultParticipantType.PATIENT);
        }
        return principal;
    }
}
