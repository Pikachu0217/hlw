package com.hlw.consult.ws;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * 问诊 WebSocket 配置。
 */
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class ConsultWebSocketConfig implements WebSocketConfigurer {
    /** 问诊 WebSocket 端点。 */
    private final ConsultWebSocketEndpoint consultWebSocketEndpoint;
    /** 问诊 WebSocket 握手拦截器。 */
    private final ConsultWebSocketHandshakeInterceptor consultWebSocketHandshakeInterceptor;

    /**
     * 注册 WebSocket 处理器。
     *
     * @param registry WebSocket 处理器注册表
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(consultWebSocketEndpoint, "/ws/consult/{consultId}")
            .addInterceptors(consultWebSocketHandshakeInterceptor)
            .setAllowedOriginPatterns("*");
    }
}
