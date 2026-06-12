package com.hlw.common.mq.model;

public record MqMessage(String topic, String body, long delayMillis, int retryCount, int maxRetry) {
}
