package com.hlw.prescription.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 审核通过处方请求。
 */
@Getter
@Setter
public class ApprovePrescriptionRequest {
    /** 审核药师编号。 */
    private Long pharmacistId;
    /** 审核备注。 */
    private String remark;
}
