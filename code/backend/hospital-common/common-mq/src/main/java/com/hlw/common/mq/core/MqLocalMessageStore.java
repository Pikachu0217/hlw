package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;

import java.util.List;

public interface MqLocalMessageStore {
    void save(MqMessage message);

    List<MqMessage> findPending(int limit);

    void markSent(MqMessage message);

    void markFailed(MqMessage message, String errorMessage);
}
