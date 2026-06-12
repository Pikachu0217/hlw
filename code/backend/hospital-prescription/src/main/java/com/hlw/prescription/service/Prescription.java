package com.hlw.prescription.service;

/**
 * 处方聚合根。
 *
 * @param id 处方编号
 * @param status 处方状态
 * @param pharmacistId 审核药师编号
 * @param auditRemark 审核备注
 */
public record Prescription(Long id, PrescriptionStatus status, Long pharmacistId, String auditRemark) {
    /**
     * 生成审核通过后的处方快照。
     *
     * @param pharmacistId 审核药师编号
     * @param auditRemark 审核备注
     * @return 审核通过后的处方
     */
    public Prescription audited(Long pharmacistId, String auditRemark) {
        return new Prescription(id, PrescriptionStatus.AUDITED, pharmacistId, auditRemark);
    }
}
