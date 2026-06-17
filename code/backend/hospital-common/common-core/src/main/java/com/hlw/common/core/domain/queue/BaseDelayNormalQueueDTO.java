package com.hlw.common.core.domain.queue;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 普通延迟队列消息基类，可由具体业务消息继承使用。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BaseDelayNormalQueueDTO extends BaseDelayQueueDTO {

    private static final long serialVersionUID = 1L;
}
