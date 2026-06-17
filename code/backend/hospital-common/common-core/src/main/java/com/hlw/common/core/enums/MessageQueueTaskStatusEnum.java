package com.hlw.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 消息队列失败任务状态。
 */
@Getter
@AllArgsConstructor
public enum MessageQueueTaskStatusEnum {

    ERROR(0, "失败待重试"),

    RUNNING(1, "重新归队中"),

    SUCCESS(2, "归队成功"),
    ;

    private final Integer status;

    private final String desc;
}
