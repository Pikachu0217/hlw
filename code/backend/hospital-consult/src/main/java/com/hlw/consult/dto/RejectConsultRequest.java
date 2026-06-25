package com.hlw.consult.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 拒诊请求。
 */
@Getter
@Setter
public class RejectConsultRequest {
    /** 拒诊原因。 */
    private String reason;
}
