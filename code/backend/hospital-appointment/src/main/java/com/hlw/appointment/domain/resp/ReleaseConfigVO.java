package com.hlw.appointment.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 放号配置展示对象。
 */
@Getter
@Setter
public class ReleaseConfigVO {
    /** 配置编号。 */
    private Long id;
    /** 排班编号。 */
    private Long scheduleId;
    /** 放号时间。 */
    private String releaseAt;
    /** 放号数量。 */
    private Integer releaseCount;
    /** 配置状态。 */
    private String status;
}
