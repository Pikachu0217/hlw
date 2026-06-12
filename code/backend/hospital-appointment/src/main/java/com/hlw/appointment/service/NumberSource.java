package com.hlw.appointment.service;

public record NumberSource(Long id, Long scheduleId, int numberSeq, NumberSourceStatus status) {
    public NumberSource locked() {
        return new NumberSource(id, scheduleId, numberSeq, NumberSourceStatus.LOCKED);
    }
}
