package com.hlw.auth.client;

/**
 * 内部患者档案 Feign 响应（与 patient 模块 InternalPatientResp 对齐）。
 *
 * @param id          患者档案编号
 * @param userId      关联用户编号（sys_user.user_id 字符串）
 * @param tenantId    租户编号
 * @param patientName 患者姓名
 */
public record InternalPatientFeignResp(Long id, String userId, Long tenantId, String patientName) {
}
