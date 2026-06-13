package com.hlw.consult.service;

public record Consult(Long id, Long tenantId, ConsultStatus status, int durationLimit, int remainingSeconds) {
    /**
     * 返回进入咨询中的问诊副本。
     *
     * @param durationLimitMinutes 问诊时长上限分钟
     * @return 咨询中问诊
     */
    public Consult inProgress(int durationLimitMinutes) {
        return new Consult(id, tenantId, ConsultStatus.IN_PROGRESS, durationLimitMinutes, durationLimitMinutes * 60);
    }

    /**
     * 返回超时状态的问诊副本。
     *
     * @return 超时问诊
     */
    public Consult timeout() {
        return new Consult(id, tenantId, ConsultStatus.TIMEOUT, durationLimit, 0);
    }
}
