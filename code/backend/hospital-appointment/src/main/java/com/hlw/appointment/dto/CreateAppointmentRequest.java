package com.hlw.appointment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 创建预约单请求。
 */
@Getter
@Setter
public class CreateAppointmentRequest {
    /** 患者编号。 */
    private Long patientId;
    /** 医生编号。 */
    private Long doctorId;
    /** 科室编号。 */
    private Long departmentId;
    /** 排班编号。 */
    @Min(value = 1, message = "排班编号必须大于 0")
    private Long scheduleId;
    /** 患者姓名。 */
    private String patientName;
    /** 医生姓名。 */
    private String doctorName;
    /** 门诊时间。 */
    private String timeSlot;
    /** 预约来源。 */
    private String source;
    /** 预约类型。 */
    private String appointmentType;
    /** 预约费用。 */
    @DecimalMin(value = "0", message = "预约费用不能小于 0")
    private BigDecimal feeAmount;
    /** 患者问题描述。 */
    private String chiefComplaint;
}
