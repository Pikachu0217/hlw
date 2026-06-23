package com.hlw.consult.client.resp;

/**
 * 内部医生信息响应。
 *
 * @param id 医生编号
 * @param userId 关联用户编号
 * @param tenantId 租户编号
 * @param doctorName 医生姓名
 */
public record InternalDoctorResp(Long id, Long userId, Long tenantId, String doctorName) {
}
