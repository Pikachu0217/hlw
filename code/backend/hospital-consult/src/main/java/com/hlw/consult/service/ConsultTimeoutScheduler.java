package com.hlw.consult.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 问诊超时调度器，定时扫描咨询中的问诊并处理超时或临近提醒。
 */
@Component
@EnableScheduling
public class ConsultTimeoutScheduler {
    private static final Logger log = LoggerFactory.getLogger(ConsultTimeoutScheduler.class);

    private final ConsultRepository consultRepository;
    private final ConsultTimeoutWarningPublisher warningPublisher;

    public ConsultTimeoutScheduler(ConsultRepository consultRepository, ConsultTimeoutWarningPublisher warningPublisher) {
        this.consultRepository = consultRepository;
        this.warningPublisher = warningPublisher;
    }

    /**
     * 每 30 秒扫描一次咨询中问诊，处理超时或五分钟提醒。
     */
    @Scheduled(fixedDelay = 30_000)
    public void scanTimeouts() {
        List<Consult> consults = consultRepository.findInProgress();
        for (Consult consult : consults) {
            if (consult.remainingSeconds() <= 0) {
                log.info("问诊超时，自动完成，consultId={}", consult.id());
                consultRepository.save(consult.timeout());
            } else if (consult.remainingSeconds() <= 300) {
                warningPublisher.publishFiveMinuteWarning(consult.id());
            }
        }
    }
}
