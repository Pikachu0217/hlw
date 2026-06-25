package com.hlw.consult.ws;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hlw.common.core.exception.BizException;
import com.hlw.consult.entity.ConConsultEntity;
import com.hlw.consult.mapper.ConConsultMapper;
import com.hlw.consult.service.ConsultDisplayStatus;
import com.hlw.consult.service.ConsultMessageType;
import com.hlw.consult.service.ConsultParticipantType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * 问诊 WebSocket 消息处理器。
 */
@Component
public class ConsultMessageHandler {
    private final ConsultMessageRepository messageRepository;
    private final ConConsultMapper conConsultMapper;
    private final ObjectMapper objectMapper;

    /**
     * 构造问诊消息处理器。
     *
     * @param messageRepository 问诊消息仓储
     * @param conConsultMapper 问诊单数据访问组件
     * @param objectMapper JSON 转换器
     */
    public ConsultMessageHandler(
        ConsultMessageRepository messageRepository,
        ConConsultMapper conConsultMapper,
        ObjectMapper objectMapper
    ) {
        this.messageRepository = messageRepository;
        this.conConsultMapper = conConsultMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 处理 WebSocket 消息并写入问诊消息仓储。
     *
     * @param consultId 问诊编号
     * @param senderId 发送人编号
     * @param senderType 发送人类型
     * @param json 原始消息 JSON
     * @return 待广播消息 JSON
     */
    public String handle(Long consultId, Long senderId, String senderType, String json) {
        JsonNode root = parseJson(json);
        String content = textValue(root, "content");
        String contentType = textValue(root, "contentType");
        if (content == null || content.isBlank()) {
            throw new BizException(400, "消息内容不能为空");
        }
        if (contentType == null || contentType.isBlank()) {
            contentType = ConsultMessageType.TEXT;
        }
        contentType = contentType.trim().toUpperCase(Locale.ROOT);
        if (!ConsultMessageType.isSendable(contentType)) {
            throw new BizException(400, "消息类型不支持");
        }
        String resolvedSenderType = senderType == null || senderType.isBlank() ? ConsultParticipantType.PATIENT : senderType;
        validateSendable(consultId, resolvedSenderType);
        ConsultMessage message = new ConsultMessage(
            null,
            consultId,
            senderId,
            resolvedSenderType,
            content,
            contentType,
            false,
            LocalDateTime.now()
        );
        return toJson(messageRepository.save(message));
    }

    /**
     * 校验当前问诊状态是否允许发送消息。
     *
     * @param consultId 问诊编号
     * @param senderType 发送人类型
     */
    private void validateSendable(Long consultId, String senderType) {
        ConConsultEntity consult = conConsultMapper.selectOne(new LambdaQueryWrapper<ConConsultEntity>()
            .eq(ConConsultEntity::getId, consultId)
            .eq(ConConsultEntity::getDeleted, 0)
            .last("limit 1"));
        if (consult == null) {
            throw new BizException(404, "问诊单不存在");
        }
        boolean accepted = ConsultDisplayStatus.IN_PROGRESS.equals(consult.getStatus())
            || ConsultDisplayStatus.EXTENDED.equals(consult.getStatus());
        if (!accepted) {
            throw new BizException(409, ConsultParticipantType.PATIENT.equals(senderType)
                ? "医生接诊后患者才能发送消息"
                : "问诊接单后才能发送消息");
        }
    }

    /**
     * 解析 JSON 文本。
     *
     * @param json 原始 JSON 文本
     * @return JSON 节点
     */
    private JsonNode parseJson(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (JsonProcessingException exception) {
            throw new BizException(400, "消息格式必须为 JSON");
        }
    }

    /**
     * 读取文本字段。
     *
     * @param root JSON 节点
     * @param fieldName 字段名
     * @return 文本值
     */
    private String textValue(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        return node == null || node.isNull() ? null : node.asText();
    }

    /**
     * 转换广播消息 JSON。
     *
     * @param message 问诊消息
     * @return 广播消息 JSON
     */
    private String toJson(ConsultMessage message) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", message.id());
        payload.put("consultId", message.consultId());
        payload.put("senderId", message.senderId());
        payload.put("senderType", message.senderType());
        payload.put("content", message.content());
        payload.put("contentType", message.contentType());
        payload.put("read", message.read());
        payload.put("createTime", message.createTime() == null ? null : message.createTime().toString());
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new BizException(500, "消息序列化失败");
        }
    }
}
