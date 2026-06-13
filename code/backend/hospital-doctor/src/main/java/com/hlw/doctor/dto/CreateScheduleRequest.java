package com.hlw.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建排班请求。
 */
@Getter
@Setter
public class CreateScheduleRequest {
    /** 医生编号。 */
    @NotNull(message = "医生编号不能为空")
    private Long doctorId;
    /** 出诊时段。 */
    @NotBlank(message = "排班时段不能为空")
    private String slot;
    /** 排班日期。 */
    private String scheduleDate;
    /** 时间段。 */
    private String timeSlot;
    /** 总号源数量。 */
    private Integer totalNumber;
    /** 剩余号源数量。 */
    private Integer remainNumber;
}
