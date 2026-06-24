package com.hlw.doctor.client;

import lombok.Getter;
import lombok.Setter;

/**
 * 创建放号配置请求（与 appointment 模块 InternalCreateReleaseConfigRequest 字段对齐）。
 */
@Getter
@Setter
public class InternalCreateReleaseConfigRequest {
    /** 排班编号。 */
    private Long scheduleId;
    /** 放号数量。 */
    private Integer releaseCount;
}
