package com.hlw.consult.ws;

import java.time.LocalDateTime;

/**
 * 问诊消息传输对象。
 *
 * @param id 消息编号
 * @param consultId 问诊编号
 * @param senderId 发送人编号
 * @param senderType 发送人类型
 * @param content 消息内容
 * @param contentType 消息内容类型
 * @param read 是否已读
 * @param createTime 创建时间
 */
public record ConsultMessage(
    Long id,
    Long consultId,
    Long senderId,
    String senderType,
    String content,
    String contentType,
    boolean read,
    LocalDateTime createTime
) {
}
