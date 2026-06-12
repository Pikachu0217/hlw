package com.hlw.patient.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PatientProfileServiceTest {
    @Test
    void patient_can_update_masked_profile_fields() {
        PatientProfileService service = new PatientProfileService(new InMemoryPatientRepository());

        PatientProfile profile = service.updateProfile(1L, new UpdatePatientProfileCommand("张三", "13812345678", "男"));

        assertThat(profile.name()).isEqualTo("张三");
        assertThat(profile.maskedPhone()).isEqualTo("138****5678");
    }
}
