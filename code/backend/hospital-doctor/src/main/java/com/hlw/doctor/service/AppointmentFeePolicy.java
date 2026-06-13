package com.hlw.doctor.service;

import java.math.BigDecimal;
import java.util.Map;

public class AppointmentFeePolicy {
    private static final Map<String, BigDecimal> TITLE_DEFAULTS = Map.of(
        "主任医师", new BigDecimal("50.00"),
        "副主任医师", new BigDecimal("30.00"),
        "主治医师", new BigDecimal("20.00"),
        "住院医师", new BigDecimal("10.00")
    );

    /**
     * 根据科室、医生和职称优先级计算挂号费。
     *
     * @param context 挂号费上下文
     * @return 挂号费
     */
    public BigDecimal resolve(FeeContext context) {
        if (context.departmentFee() != null) {
            return context.departmentFee();
        }
        if (context.doctorFee() != null) {
            return context.doctorFee();
        }
        return TITLE_DEFAULTS.getOrDefault(context.title(), BigDecimal.ZERO);
    }
}
