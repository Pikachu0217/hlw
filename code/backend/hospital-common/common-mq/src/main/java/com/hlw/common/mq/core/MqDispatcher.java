package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;

public interface MqDispatcher {
    void dispatch(MqMessage message);
}
