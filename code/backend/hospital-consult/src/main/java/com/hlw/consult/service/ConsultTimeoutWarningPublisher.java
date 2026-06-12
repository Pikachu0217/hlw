package com.hlw.consult.service;

@FunctionalInterface
public interface ConsultTimeoutWarningPublisher {
    void publishFiveMinuteWarning(Long consultId);
}
