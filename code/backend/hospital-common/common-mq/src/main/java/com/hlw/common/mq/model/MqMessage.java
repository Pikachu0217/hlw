package com.hlw.common.mq.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 普通消息类，用于封装本地消息队列的基础属性。
 */
@Data
@NoArgsConstructor
public class MqMessage {
    // 消息的唯一标识符
    private String id;
    // 消息所属的主题
    private String topic;
    // 消息的具体内容
    private String body;
    // 消息已重试的次数
    private int retryCount;
    // 消息最大允许重试的次数
    private int maxRetry;

    /**
     * 构造普通消息。
     *
     * @param topic 消息主题
     * @param body 消息内容
     * @param retryCount 已重试次数
     * @param maxRetry 最大重试次数
     */
    public MqMessage(String topic, String body, int retryCount, int maxRetry) {
        this.topic = topic;
        this.body = body;
        this.retryCount = retryCount;
        this.maxRetry = maxRetry;
    }
}
