package com.hlw.consult.client.resp;

/**
 * 预约单内部响应（从 appointment 模块 Feign 查询返回）。
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
