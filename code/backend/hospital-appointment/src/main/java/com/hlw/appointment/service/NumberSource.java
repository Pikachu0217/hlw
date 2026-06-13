package com.hlw.appointment.service;

public record NumberSource(Long id, Long scheduleId, int numberSeq, NumberSourceStatus status) {
    /**
     * 返回锁定状态的号源副本。
     *
     * @return 锁定后的号源
     */
    public NumberSource locked() {
        return new NumberSource(id, scheduleId, numberSeq, NumberSourceStatus.LOCKED);
    }
}
