package com.hlw.prescription.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 处方展示对象。
 */
@Getter
@Setter
public class PrescriptionVO {    /** 处方编号。 */
    private Long id;
    /** 处方号。 */
    private String prescriptionNo;
    /** 患者姓名。 */
    private String patientName;
    /** 医生姓名。 */
    private String doctorName;
    /** 药品数量。 */
    private Integer drugCount;
    /** 开方时间展示值。 */
    private String issuedAt;
    /** 处方状态。 */
    private String status;
    /** 审核备注。 */
    private String remark;
}
