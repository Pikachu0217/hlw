package com.hlw.common.core.domain.queue;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 延迟队列消息基类，扩展业务延迟时间字段。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseDelayQueueDTO extends BaseQueueDTO {

    private static final long serialVersionUID = 1L;

    /**
     * 触发执行时间（毫秒时间戳）。
     */
    private Long executeTime;
}
