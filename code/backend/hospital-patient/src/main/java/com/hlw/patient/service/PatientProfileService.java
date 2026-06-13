package com.hlw.patient.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 患者档案服务，负责读取、更新患者基础资料并处理手机号脱敏。
 */
public class PatientProfileService {
    private static final Logger log = LoggerFactory.getLogger(PatientProfileService.class);

    private final PatientRepository patientRepository;

    /**
     * 构造患者档案服务。
     *
     * @param patientRepository 患者仓储
     */
    public PatientProfileService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    /**
     * 查询患者档案。
     *
     * @param patientId 患者编号
     * @return 患者档案
     */
    public PatientProfile findProfile(Long patientId) {
        log.info("查询患者档案，patientId={}", patientId);
        return patientRepository.findById(patientId);
    }

    /**
     * 更新患者档案。
     *
     * @param patientId 患者编号
     * @param command 更新命令
     * @return 更新后的患者档案
     */
    public PatientProfile updateProfile(Long patientId, UpdatePatientProfileCommand command) {
        log.info("更新患者档案，patientId={}，name={}", patientId, command.name());
        return patientRepository.save(patientId, command, maskPhone(command.phone()));
    }

    /**
     * 手机号脱敏。
     *
     * @param phone 手机号
     * @return 脱敏手机号
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
