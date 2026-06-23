package com.hlw.auth.domain.req;

/**
 * 内部创建患者用户请求（Feign 传输用，字段与 system 模块 CreatePatientUserInternalReq 对齐）。
 *
 * @param tenantId 租户编号
 * @param userName 登录账号
 * @param phone    联系电话
 */
public record CreatePatientUserFeignReq(Long tenantId, String userName, String phone) {
}
