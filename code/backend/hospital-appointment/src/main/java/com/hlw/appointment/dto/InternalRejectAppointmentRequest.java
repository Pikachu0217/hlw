package com.hlw.appointment.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 内部拒诊预约同步请求。
 */
@Getter
@Setter
public class InternalRejectAppointmentRequest {
    /** 拒诊原因。 */
    private String reason;
}
