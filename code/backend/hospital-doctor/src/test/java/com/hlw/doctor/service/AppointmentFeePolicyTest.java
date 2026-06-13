package com.hlw.doctor.service;

import com.hlw.doctor.dto.ResolveAppointmentFeeRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentFeePolicyTest {
    @Test
    void department_fee_overrides_doctor_and_title_defaults() {
        AppointmentFeePolicyService policy = new AppointmentFeePolicyService();

        BigDecimal fee = policy.resolve(
            request("主任医师", new BigDecimal("80.00"), new BigDecimal("20.00"))
        );

        assertThat(fee).isEqualByComparingTo("20.00");
    }

    @Test
    void doctor_fee_overrides_title_default_when_department_fee_absent() {
        AppointmentFeePolicyService policy = new AppointmentFeePolicyService();

        BigDecimal fee = policy.resolve(
            request("主任医师", new BigDecimal("80.00"), null)
        );

        assertThat(fee).isEqualByComparingTo("80.00");
    }

    /**
     * 构造挂号费计算请求。
     *
     * @param title 医生职称
     * @param doctorFee 医生挂号费
     * @param departmentFee 科室挂号费
     * @return 请求对象
     */
    private ResolveAppointmentFeeRequest request(String title, BigDecimal doctorFee, BigDecimal departmentFee) {
        ResolveAppointmentFeeRequest request = new ResolveAppointmentFeeRequest();
        request.setTitle(title);
        request.setDoctorFee(doctorFee);
        request.setDepartmentFee(departmentFee);
        return request;
    }
}
