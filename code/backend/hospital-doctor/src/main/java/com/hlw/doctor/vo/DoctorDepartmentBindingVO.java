package com.hlw.doctor.vo;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 医生科室绑定展示对象。
 */
@Getter
@Setter
public class DoctorDepartmentBindingVO {
    /** 表格主键。 */
    private String key;
    /** 医生编号。 */
    private Long doctorId;
    /** 科室编号。 */
    private Long departmentId;
    /** 是否免挂号费。 */
    private Boolean free;
    /** 挂号费用。 */
    private BigDecimal appointmentFee;
}
