package com.hlw.consult.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 接单问诊请求。
 */
@Getter
@Setter
public class AcceptConsultRequest {
    /** 医生编号。 */
    private Long doctorId;
}
