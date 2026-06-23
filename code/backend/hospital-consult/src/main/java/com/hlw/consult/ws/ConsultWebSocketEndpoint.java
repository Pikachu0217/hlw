package com.hlw.consult.ws;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 问诊 WebSocket 端点。
 */
@Component
@Slf4j
public class ConsultWebSocketEndpoint extends TextWebSocketHandler {
    private static final String CONSULT_ID_ATTRIBUTE = "consultId";
    private static final String SENDER_ID_ATTRIBUTE = "senderId";
    private static final String SENDER_TYPE_ATTRIBUTE = "senderType";

    /** 问诊编号与会话集合映射。 */
    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();
    /** 问诊消息处理器。 */
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
     * 连接建立后加入问诊房间。
     *
     * @param session WebSocket 会话
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Long consultId = sessionLongAttribute(session, CONSULT_ID_ATTRIBUTE);
        roomSessions.computeIfAbsent(consultId, ignored -> ConcurrentHashMap.newKeySet()).add(session);
        log.info("问诊 WebSocket 连接建立，consultId={}，sessionId={}", consultId, session.getId());
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
        Long consultId = sessionLongAttribute(session, CONSULT_ID_ATTRIBUTE);
        Long senderId = sessionLongAttribute(session, SENDER_ID_ATTRIBUTE);
        String senderType = String.valueOf(session.getAttributes().get(SENDER_TYPE_ATTRIBUTE));
        String broadcast = messageHandler.handle(consultId, senderId, senderType, message.getPayload());
        broadcast(consultId, broadcast);
    }

    /**
     * 处理 WebSocket 连接关闭事件。
     *
     * @param session WebSocket 会话
     * @param status 关闭状态
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        Long consultId = sessionLongAttribute(session, CONSULT_ID_ATTRIBUTE);
        Set<WebSocketSession> sessions = roomSessions.get(consultId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) {
                roomSessions.remove(consultId);
            }
        }
        log.info("问诊 WebSocket 连接关闭，consultId={}，sessionId={}，status={}", consultId, session.getId(), status);
    }

    /**
     * 广播消息到问诊房间。
     *
     * @param consultId 问诊编号
     * @param payload 广播内容
     */
    private void broadcast(Long consultId, String payload) {
        Set<WebSocketSession> sessions = roomSessions.getOrDefault(consultId, Set.of());
        sessions.forEach(session -> send(session, payload));
        log.info("问诊 WebSocket 广播消息，consultId={}，sessionCount={}", consultId, sessions.size());
    }

    /**
     * 向单个会话发送消息。
     *
     * @param session WebSocket 会话
     * @param payload 消息内容
     */
    private void send(WebSocketSession session, String payload) {
        if (!session.isOpen()) {
            return;
        }
        try {
            session.sendMessage(new TextMessage(payload));
        } catch (IOException exception) {
            log.warn("问诊 WebSocket 发送消息失败，sessionId={}", session.getId(), exception);
        }
    }

    /**
     * 读取会话长整型属性。
     *
     * @param session WebSocket 会话
     * @param attributeName 属性名
     * @return 属性值
     */
    private Long sessionLongAttribute(WebSocketSession session, String attributeName) {
        Object value = session.getAttributes().get(attributeName);
        return value instanceof Long ? (Long) value : Long.valueOf(String.valueOf(value));
    }
}
