package com.hlw.consult.ws;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 问诊 WebSocket 消息处理器。
 */
@Component
public class ConsultMessageHandler {
    private final ConsultMessageRepository messageRepository;

    /**
     * 构造问诊消息处理器。
     *
     * @param messageRepository 问诊消息仓储
     */
    public ConsultMessageHandler(ConsultMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * 处理 WebSocket 消息并写入问诊消息仓储。
     *
     * @param consultId 问诊编号
     * @param senderId 发送人编号
     * @param json 原始消息 JSON
     * @return 待广播消息
     */
    public String handle(Long consultId, Long senderId, String json) {
        String content = extractJsonValue(json, "content");
        String contentType = extractJsonValue(json, "contentType");
        String senderType = extractJsonValue(json, "senderType");
        if (senderType == null || senderType.isBlank()) {
            senderType = "PATIENT";
        }
        ConsultMessage message = new ConsultMessage(
            consultId,
            senderId,
            senderType,
            content,
            contentType,
            false,
            LocalDateTime.now()
        );
        messageRepository.save(message);
        return json;
    }

    /**
     * 从简单 JSON 文本中提取字段值。
     *
     * @param json 原始 JSON 文本
     * @param fieldName 字段名
     * @return 字段值
     */
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
