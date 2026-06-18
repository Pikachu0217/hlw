package com.hlw.appointment.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 预约号源展示对象。
 */
@Getter
@Setter
public class NumberSourceVO {
    /** 号源编号。 */
    private Long id;
    /** 排班编号。 */
    private Long scheduleId;
    /** 号源序号。 */
    private Integer numberSeq;
    /** 号源状态。 */
    private String status;
}
