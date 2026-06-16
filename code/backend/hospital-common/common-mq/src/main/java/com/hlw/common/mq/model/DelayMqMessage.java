package com.hlw.common.mq.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 延迟消息类，用于表示需要延迟发送的消息。
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DelayMqMessage extends MqMessage {
    // 延迟时间，单位为毫秒
    private long delayMillis;

    /**
     * 构造延迟消息。
     *
     * @param topic 消息主题
     * @param body 消息内容
     * @param delayMillis 延迟时间，单位为毫秒
     * @param retryCount 已重试次数
     * @param maxRetry 最大重试次数
     */
    public DelayMqMessage(String topic, String body, long delayMillis, int retryCount, int maxRetry) {
        super(topic, body, retryCount, maxRetry);
        this.delayMillis = delayMillis;
    }
}
