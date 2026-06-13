package com.hlw.appointment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 抢预约单请求。
 */
@Getter
@Setter
public class GrabAppointmentRequest {
    /** 医生编号。 */
    @NotNull(message = "医生编号不能为空")
    private Long doctorId;
}
