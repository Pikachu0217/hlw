package com.hlw.doctor.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentFeePolicyTest {
    @Test
    void department_fee_overrides_doctor_and_title_defaults() {
        AppointmentFeePolicy policy = new AppointmentFeePolicy();

        BigDecimal fee = policy.resolve(
            new FeeContext("主任医师", new BigDecimal("80.00"), new BigDecimal("20.00"))
        );

        assertThat(fee).isEqualByComparingTo("20.00");
    }

    @Test
    void doctor_fee_overrides_title_default_when_department_fee_absent() {
        AppointmentFeePolicy policy = new AppointmentFeePolicy();

        BigDecimal fee = policy.resolve(
            new FeeContext("主任医师", new BigDecimal("80.00"), null)
        );

        assertThat(fee).isEqualByComparingTo("80.00");
    }
}
