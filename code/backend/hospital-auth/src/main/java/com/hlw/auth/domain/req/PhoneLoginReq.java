package com.hlw.auth.domain.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 手机号登录请求，承载手机号与验证码参数。
 *
 * @param phone   手机号
 * @param smsCode 短信验证码
 */
public record PhoneLoginReq(
        @NotBlank(message = "手机号不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        String phone,

        @NotBlank(message = "验证码不能为空")
        String smsCode) {
}
