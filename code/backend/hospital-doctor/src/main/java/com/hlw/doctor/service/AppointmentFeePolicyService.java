package com.hlw.doctor.service;

import com.hlw.doctor.dto.ResolveAppointmentFeeRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * 挂号费策略服务。
 */
@Service
public class AppointmentFeePolicyService {
    private static final Map<String, BigDecimal> TITLE_DEFAULTS = Map.of(
        "主任医师", new BigDecimal("50.00"),
        "副主任医师", new BigDecimal("30.00"),
        "主治医师", new BigDecimal("20.00"),
        "住院医师", new BigDecimal("10.00")
    );

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
        return TITLE_DEFAULTS.getOrDefault(request.getTitle(), BigDecimal.ZERO);
    }
}
