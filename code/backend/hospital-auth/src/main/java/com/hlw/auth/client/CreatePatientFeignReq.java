package com.hlw.auth.client;

/**
 * 创建患者档案 Feign 请求。
 *
 * @param tenantId 租户编号
 * @param userId   关联用户编号（sys_user.user_id 字符串）
 * @param phone    联系电话
 */
public record CreatePatientFeignReq(Long tenantId, String userId, String phone) {
}
