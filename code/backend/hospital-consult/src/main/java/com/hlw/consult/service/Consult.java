package com.hlw.consult.service;

public record Consult(Long id, Long tenantId, ConsultStatus status, int durationLimit, int remainingSeconds) {
    public Consult inProgress(int durationLimitMinutes) {
        return new Consult(id, tenantId, ConsultStatus.IN_PROGRESS, durationLimitMinutes, durationLimitMinutes * 60);
    }

    public Consult timeout() {
        return new Consult(id, tenantId, ConsultStatus.TIMEOUT, durationLimit, 0);
    }
}
