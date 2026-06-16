package com.hlw.common.core.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Date 2026/6/16 16:01
 * @Created by pakachuzy
 * @Description 删除状态枚举
 */
@Getter
@AllArgsConstructor
public enum DeletedStatusEnum {
    NOT_DELETED(0, "未删除"),
    DELETED(1, "已删除"),
    ;
    private final Integer type;
    private final String desc;
}
