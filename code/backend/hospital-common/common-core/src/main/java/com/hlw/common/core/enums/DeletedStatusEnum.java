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
    DELETED(1, "已删除"),
    NOT_DELETED(0, "未删除"),
    ;
    private Integer type;
    private String desc;
}
