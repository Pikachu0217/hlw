package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;

import java.time.Duration;

public final class MqRetryPolicy {
    private MqRetryPolicy() {
    }

    /**
     * 根据重试次数计算下一次重试延迟。
     *
     * @param message 消息体
     * @return 下一次重试延迟
     */
    public static Duration nextDelay(MqMessage message) {
        long seconds = (long) Math.pow(2, Math.max(0, message.retryCount()));
        return Duration.ofSeconds(seconds);
    }
}
