package com.hlw.prescription.service;

/**
 * 处方状态枚举。
 */
public enum PrescriptionStatus {
    /** 草稿。 */
    DRAFT,
    /** 已提交待审。 */
    SUBMITTED,
    /** 审核通过。 */
    AUDITED,
    /** 已驳回。 */
    REJECTED
}
