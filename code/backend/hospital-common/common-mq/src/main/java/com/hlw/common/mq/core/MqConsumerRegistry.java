package com.hlw.common.mq.core;

import java.util.function.Consumer;

public interface MqConsumerRegistry {
    void register(String topic, Consumer<String> consumer);
}
