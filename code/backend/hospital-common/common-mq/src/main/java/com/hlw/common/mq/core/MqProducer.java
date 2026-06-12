package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;

public interface MqProducer {
    void publish(MqMessage message);
}
