package com.hlw.common.mq.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息队列推送方法枚举，用于失败任务归队时反射调用对应方法。
 */
@Getter
@AllArgsConstructor
public enum MessageQueueMethodEnum {

    SEND("send", "普通队列消息推送"),

    BROAD_SEND("broadSend", "广播队列消息推送"),

    PRIORITY_SEND("prioritySend", "优先级/延迟队列消息推送"),
    ;

    private final String method;

    private final String methodName;
}
