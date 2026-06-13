package com.hlw.consult.service;

import java.util.List;

public class ConsultTimeoutScheduler {
    private final ConsultRepository consultRepository;
    private final ConsultTimeoutWarningPublisher warningPublisher;

    public ConsultTimeoutScheduler(ConsultRepository consultRepository, ConsultTimeoutWarningPublisher warningPublisher) {
        this.consultRepository = consultRepository;
        this.warningPublisher = warningPublisher;
    }

    /**
     * 扫描咨询中问诊并处理超时或五分钟提醒。
     */
    public void scanTimeouts() {
        List<Consult> consults = consultRepository.findInProgress();
        for (Consult consult : consults) {
            if (consult.remainingSeconds() <= 0) {
                consultRepository.save(consult.timeout());
            } else if (consult.remainingSeconds() <= 300) {
                warningPublisher.publishFiveMinuteWarning(consult.id());
            }
        }
    }
}
