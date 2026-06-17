package com.hlw.consult.service;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 问诊超时提醒发布器默认实现（记录日志）。
 */
@Component
@Slf4j
public class LoggingTimeoutWarningPublisher implements ConsultTimeoutWarningPublisher {
    @Override
    public void publishFiveMinuteWarning(Long consultId) {
        log.warn("问诊即将超时（剩余5分钟），consultId={}", consultId);
    }
}
