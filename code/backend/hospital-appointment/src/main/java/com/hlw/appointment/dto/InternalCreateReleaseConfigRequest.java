package com.hlw.appointment.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 内部创建放号配置请求（供 doctor 模块 Feign 调用，无校验）。
 */
@Getter
@Setter
public class InternalCreateReleaseConfigRequest {
    /** 排班编号。 */
    private Long scheduleId;
    /** 放号数量。 */
    private Integer releaseCount;
}
