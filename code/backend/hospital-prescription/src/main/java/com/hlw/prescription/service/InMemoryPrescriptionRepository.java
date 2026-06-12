package com.hlw.prescription.service;

import java.util.HashMap;
import java.util.Map;

/**
 * 基于内存的处方仓储实现，供当前模块骨架和测试使用。
 */
public class InMemoryPrescriptionRepository implements PrescriptionRepository {
    private final Map<Long, Prescription> prescriptions = new HashMap<>();

    /**
     * 按处方编号查询处方，不存在时返回默认待审核处方。
     *
     * @param prescriptionId 处方编号
     * @return 处方对象
     */
    @Override
    public Prescription findById(Long prescriptionId) {
        return prescriptions.getOrDefault(prescriptionId, new Prescription(prescriptionId, PrescriptionStatus.SUBMITTED, null, null));
    }

    /**
     * 保存处方快照。
     *
     * @param prescription 处方对象
     */
    @Override
    public void save(Prescription prescription) {
        prescriptions.put(prescription.id(), prescription);
    }
}
