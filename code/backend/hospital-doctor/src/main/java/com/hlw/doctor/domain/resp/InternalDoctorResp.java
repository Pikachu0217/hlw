package com.hlw.doctor.domain.resp;

/**
 * 内部医生信息响应。
 *
 * @param id 医生编号
 * @param userId 关联用户编号（sys_user.user_id 字符串）
 * @param tenantId 租户编号
 * @param doctorName 医生姓名
 */
public record InternalDoctorResp(Long id, String userId, Long tenantId, String doctorName) {
}
