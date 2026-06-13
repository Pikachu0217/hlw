package com.hlw.patient.service;

import java.util.HashMap;
import java.util.Map;

public class InMemoryPatientRepository implements PatientRepository {
    private final Map<Long, PatientProfile> profiles = new HashMap<>();

    @Override
    /**
     * 保存患者资料到内存仓储。
     *
     * @param patientId 患者编号
     * @param command 更新命令
     * @param maskedPhone 脱敏手机号
     * @return 保存后的患者资料
     */
    public PatientProfile save(Long patientId, UpdatePatientProfileCommand command, String maskedPhone) {
        PatientProfile profile = new PatientProfile(patientId, command.name(), maskedPhone, command.gender());
        profiles.put(patientId, profile);
        return profile;
    }

    @Override
    /**
     * 按编号查询患者资料。
     *
     * @param patientId 患者编号
     * @return 患者资料
     */
    public PatientProfile findById(Long patientId) {
        return profiles.get(patientId);
    }
}
