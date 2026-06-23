package com.hlw.patient.domain.resp;

/**
 * 内部患者信息响应。
 *
 * @param id          患者档案编号
 * @param userId      关联用户编号（sys_user.user_id 字符串）
 * @param tenantId    租户编号
 * @param patientName 患者姓名
 */
public record InternalPatientResp(Long id, String userId, Long tenantId, String patientName) {
}
