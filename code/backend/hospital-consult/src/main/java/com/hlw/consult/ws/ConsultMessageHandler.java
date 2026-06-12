package com.hlw.consult.ws;

import java.time.LocalDateTime;

public class ConsultMessageHandler {
    private final ConsultMessageRepository messageRepository;

    public ConsultMessageHandler(ConsultMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public String handle(Long consultId, Long senderId, String json) {
        String content = extractJsonValue(json, "content");
        String contentType = extractJsonValue(json, "contentType");
        ConsultMessage message = new ConsultMessage(
            consultId,
            senderId,
            "PATIENT",
            content,
            contentType,
            false,
            LocalDateTime.now()
        );
        messageRepository.save(message);
        return json;
    }

    private String extractJsonValue(String json, String fieldName) {
        String marker = "\"" + fieldName + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) {
            return null;
        }
        int valueStart = start + marker.length();
        int valueEnd = json.indexOf('"', valueStart);
        return valueEnd < 0 ? null : json.substring(valueStart, valueEnd);
    }
}
