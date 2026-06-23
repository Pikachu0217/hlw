package com.hlw.doctor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 绑定医生科室请求。
 */
@Getter
@Setter
public class BindDoctorDepartmentRequest {
    /** 科室编号。 */
    @NotNull(message = "科室编号不能为空")
    private Long deptId;
    /** 是否免挂号费。 */
    private Boolean free;
    /** 挂号费用。 */
    private BigDecimal appointmentFee;
}
