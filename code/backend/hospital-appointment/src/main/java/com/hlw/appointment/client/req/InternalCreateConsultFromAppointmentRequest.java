package com.hlw.appointment.client.req;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 内部创建预约绑定问诊请求。
 */
@Getter
@Setter
public class InternalCreateConsultFromAppointmentRequest {
    /** 预约单编号。 */
    private Long appointmentId;
    /** 患者编号。 */
    private Long patientId;
    /** 医生编号。 */
    private Long doctorId;
    /** 患者姓名。 */
    private String patientName;
    /** 医生姓名。 */
    private String doctorName;
    /** 问诊费用。 */
    private BigDecimal feeAmount;
    /** 支付状态。 */
    private String payStatus;
    /** 患者问题描述。 */
    private String chiefComplaint;
}
