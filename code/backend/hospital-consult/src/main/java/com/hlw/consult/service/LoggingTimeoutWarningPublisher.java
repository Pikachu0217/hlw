package com.hlw.consult.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * 问诊超时提醒发布器默认实现（记录日志）。
 */
@Component
public class LoggingTimeoutWarningPublisher implements ConsultTimeoutWarningPublisher {
    private static final Logger log = LoggerFactory.getLogger(LoggingTimeoutWarningPublisher.class);

    @Override
    public void publishFiveMinuteWarning(Long consultId) {
        log.warn("问诊即将超时（剩余5分钟），consultId={}", consultId);
    }
}
