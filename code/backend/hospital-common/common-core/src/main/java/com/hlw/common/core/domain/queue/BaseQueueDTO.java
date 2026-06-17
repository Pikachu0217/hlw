package com.hlw.common.core.domain.queue;

import lombok.Data;

import java.io.Serializable;

/**
 * 消息队列消息基类，承载重试与归队次数等公共字段。
 */
@Data
public class BaseQueueDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private int retryTimes;

    private int backQueueTimes;
}
