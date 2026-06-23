package com.hlw.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户类型枚举。
 */
@Getter
@AllArgsConstructor
public enum UserTypeEnum {
    /** 后台系统用户。 */
    SYS_USER("sys_user", "系统用户"),
    /** 医生工作台用户。 */
    DOCTOR("doctor", "医生"),
    /** 患者端用户。 */
    PATIENT("patient", "患者"),
    /** 药师工作台用户。 */
    PHARMACIST("pharmacist", "药师");

    /** 用户类型编码。 */
    private final String userType;
    /** 用户类型描述。 */
    private final String desc;
}
