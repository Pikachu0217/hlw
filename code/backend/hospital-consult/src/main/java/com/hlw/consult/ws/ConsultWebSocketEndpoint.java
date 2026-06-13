package com.hlw.consult.ws;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.stereotype.Component;

/**
 * 问诊 WebSocket 端点。
 */
@Component
public class ConsultWebSocketEndpoint extends TextWebSocketHandler {
    private final ConsultMessageHandler messageHandler;

    /**
     * 构造问诊 WebSocket 端点。
     *
     * @param messageHandler 问诊消息处理器
     */
    public ConsultWebSocketEndpoint(ConsultMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    /**
     * 处理 WebSocket 文本消息。
     *
     * @param session WebSocket 会话
     * @param message 文本消息
     * @throws Exception 处理异常
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long consultId = Long.valueOf(String.valueOf(session.getAttributes().get("consultId")));
        Long senderId = Long.valueOf(String.valueOf(session.getAttributes().get("senderId")));
        String broadcast = messageHandler.handle(consultId, senderId, message.getPayload());
        session.sendMessage(new TextMessage(broadcast));
    }

    /**
     * 处理 WebSocket 连接关闭事件。
     *
     * @param session WebSocket 会话
     * @param status 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }
}
