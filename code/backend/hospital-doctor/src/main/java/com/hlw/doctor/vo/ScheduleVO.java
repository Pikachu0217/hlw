package com.hlw.doctor.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 排班展示对象。
 */
@Getter
@Setter
public class ScheduleVO {
    /** 表格主键。 */
    private String key;
    /** 排班编号。 */
    private Long id;
    /** 医生编号。 */
    private Long doctorId;
    /** 医生姓名。 */
    private String doctorName;
    /** 出诊时段。 */
    private String slot;
    /** 排班日期。 */
    private String scheduleDate;
    /** 时间段。 */
    private String timeSlot;
    /** 总号源数量。 */
    private Integer totalNumber;
    /** 剩余号源数量。 */
    private Integer remain;
}
