package com.hlw.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Date 2026/6/16 16:01
 * @Created by pakachuzy
 * @Description 通用状态枚举
 */
@Getter
@AllArgsConstructor
public enum CommonStatusEnum {
    DISABLED("0", "禁用"),
    ENABLED("1", "启用"),
    ;
    private final String status;
    private final String desc;
}
