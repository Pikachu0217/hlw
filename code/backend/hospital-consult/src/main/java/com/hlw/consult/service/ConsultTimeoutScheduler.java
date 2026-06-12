package com.hlw.consult.service;

import java.util.List;

public class ConsultTimeoutScheduler {
    private final ConsultRepository consultRepository;
    private final ConsultTimeoutWarningPublisher warningPublisher;

    public ConsultTimeoutScheduler(ConsultRepository consultRepository, ConsultTimeoutWarningPublisher warningPublisher) {
        this.consultRepository = consultRepository;
        this.warningPublisher = warningPublisher;
    }

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
