package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;

import java.time.Duration;

public final class MqRetryPolicy {
    private MqRetryPolicy() {
    }

    public static Duration nextDelay(MqMessage message) {
        long seconds = (long) Math.pow(2, Math.max(0, message.retryCount()));
        return Duration.ofSeconds(seconds);
    }
}
