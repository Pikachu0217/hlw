package com.hlw.prescription.service;

/**
 * 处方仓储接口。
 */
public interface PrescriptionRepository {
    /**
     * 按处方编号查询处方。
     *
     * @param prescriptionId 处方编号
     * @return 处方对象
     */
    Prescription findById(Long prescriptionId);

    /**
     * 保存处方。
     *
     * @param prescription 处方对象
     */
    void save(Prescription prescription);
}
