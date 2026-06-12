package com.hlw.patient.config;

import com.hlw.patient.service.InMemoryPatientRepository;
import com.hlw.patient.service.PatientProfileService;
import com.hlw.patient.service.PatientRepository;
import com.hlw.patient.service.UpdatePatientProfileCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 患者模块本地启动默认配置。
 */
@Configuration
public class PatientServiceConfig {
    /**
     * 创建患者档案服务。
     *
     * @param patientRepository 患者仓储
     * @return 患者档案服务
     */
    @Bean
    public PatientProfileService patientProfileService(PatientRepository patientRepository) {
        PatientProfileService service = new PatientProfileService(patientRepository);
        service.updateProfile(1L, new UpdatePatientProfileCommand("王小雨", "13812345678", "女"));
        return service;
    }

    /**
     * 创建内存患者仓储。
     *
     * @return 患者仓储
     */
    @Bean
    public PatientRepository patientRepository() {
        return new InMemoryPatientRepository();
    }
}
