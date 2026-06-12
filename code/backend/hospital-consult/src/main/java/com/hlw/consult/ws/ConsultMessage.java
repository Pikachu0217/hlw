package com.hlw.consult.ws;

import java.time.LocalDateTime;

public record ConsultMessage(
    Long consultId,
    Long senderId,
    String senderType,
    String content,
    String contentType,
    boolean read,
    LocalDateTime createTime
) {
}
