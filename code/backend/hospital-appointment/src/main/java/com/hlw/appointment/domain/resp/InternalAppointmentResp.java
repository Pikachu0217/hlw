package com.hlw.appointment.domain.resp;

/**
 * 预约单内部响应对象（供 Feign 调用方使用）。
 */
public record InternalAppointmentResp(
    Long id,
    Long patientId,
    Long doctorId,
    String patientName,
    String doctorName,
    String feeAmount
) {
}
