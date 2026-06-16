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
    ENABLED("0", "启用"),
    DISABLED("1", "禁用"),
    ;
    private String status;
    private String desc;
}
