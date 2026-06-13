package com.hlw.patient.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PatientTenantContextServiceTest {
    /**
     * 校验患者手机号脱敏规则。
     */
    @Test
    void patient_phone_should_be_masked() {
        PatientTenantContextService service = new PatientTenantContextService(null, null);

        assertThat(service.maskPhone("13812345678")).isEqualTo("138****5678");
        assertThat(service.maskPhone("12345")).isEqualTo("12345");
    }
}
