package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class LocalMqRetryTest {
    @Test
    void failed_message_uses_exponential_backoff_until_max_retry() {
        MqMessage message = new MqMessage("order.paid", "{\"orderId\":1}", 0, 1, 3);

        Duration nextDelay = MqRetryPolicy.nextDelay(message);

        assertThat(nextDelay).isEqualTo(Duration.ofSeconds(2));
    }
}
