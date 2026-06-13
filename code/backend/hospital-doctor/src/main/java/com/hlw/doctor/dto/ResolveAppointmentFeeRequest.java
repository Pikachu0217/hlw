package com.hlw.doctor.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 计算挂号费请求。
 */
@Getter
@Setter
public class ResolveAppointmentFeeRequest {
    /** 医生职称。 */
    private String title;
    /** 医生挂号费。 */
    private BigDecimal doctorFee;
    /** 科室挂号费。 */
    private BigDecimal departmentFee;
}
