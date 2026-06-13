package com.hlw.prescription.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 驳回处方请求。
 */
@Getter
@Setter
public class RejectPrescriptionRequest {
    /** 驳回备注。 */
    private String remark;
}
