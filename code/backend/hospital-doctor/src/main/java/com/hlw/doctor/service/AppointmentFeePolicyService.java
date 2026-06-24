package com.hlw.doctor.service;

import com.hlw.doctor.dto.ResolveAppointmentFeeRequest;
import com.hlw.doctor.enums.DoctorJobTitleFeeEnum;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 挂号费策略服务。
 */
@Service
public class AppointmentFeePolicyService {
    /**
     * 根据科室、医生和职称优先级计算挂号费。
     *
     * @param request 挂号费上下文
     * @return 挂号费
     */
    public BigDecimal resolve(ResolveAppointmentFeeRequest request) {
        if (request.getDepartmentFee() != null) {
            return request.getDepartmentFee();
        }
        if (request.getDoctorFee() != null) {
            return request.getDoctorFee();
        }
        return DoctorJobTitleFeeEnum.resolveFee(request.getTitle());
    }

    /**
     * 根据医生职称计算默认问诊费用。
     *
     * @param title 医生职称
     * @return 默认问诊费用
     */
    public BigDecimal resolveByTitle(String title) {
        return DoctorJobTitleFeeEnum.resolveFee(title);
    }
}
