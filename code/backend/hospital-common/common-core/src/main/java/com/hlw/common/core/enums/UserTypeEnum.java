package com.hlw.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Date 2026/6/16 16:01
 * @Created by pakachuzy
 * @Description 用户状态枚举
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {
    PLATFORM_ADMIN(1, "平台管理员"),
    TENANT_ADMIN(2, "租户管理员"),
    DOCTOR(3, "医生"),
    NURSE(4, "护士"),
    PATIENT(5, "患者"),
    ;
    private Integer userType;
    private String desc;
}
