package com.hlw.patient.config;

import com.hlw.patient.service.JdbcPatientRepository;
import com.hlw.patient.service.PatientProfileService;
import com.hlw.patient.service.PatientRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcOperations;

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
        return new PatientProfileService(patientRepository);
    }

    /**
     * 创建 JDBC 患者仓储。
     *
     * @param jdbcOperations JDBC 操作组件
     * @return 患者仓储
     */
    @Bean
    public PatientRepository patientRepository(JdbcOperations jdbcOperations) {
        return new JdbcPatientRepository(jdbcOperations);
    }
}
