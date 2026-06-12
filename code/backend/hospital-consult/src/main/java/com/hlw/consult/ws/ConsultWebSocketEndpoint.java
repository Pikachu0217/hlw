package com.hlw.consult.ws;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ConsultWebSocketEndpoint extends TextWebSocketHandler {
    private final ConsultMessageHandler messageHandler;

    public ConsultWebSocketEndpoint(ConsultMessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long consultId = Long.valueOf(String.valueOf(session.getAttributes().get("consultId")));
        Long senderId = Long.valueOf(String.valueOf(session.getAttributes().get("senderId")));
        String broadcast = messageHandler.handle(consultId, senderId, message.getPayload());
        session.sendMessage(new TextMessage(broadcast));
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    }
}
