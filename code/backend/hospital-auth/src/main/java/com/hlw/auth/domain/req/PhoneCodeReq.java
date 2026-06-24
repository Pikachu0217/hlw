package com.hlw.auth.domain.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 发送验证码请求，承载手机号参数。
 *
 * @param phone    手机号
 * @param tenantId 租户编号
 */
public record PhoneCodeReq(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        Long tenantId) {

    /**
     * 返回携带指定租户编号的新验证码请求。
     *
     * @param tenantId 租户编号
     * @return 验证码请求
     */
    public PhoneCodeReq withTenantId(Long tenantId) {
        return new PhoneCodeReq(phone, tenantId);
    }
}
