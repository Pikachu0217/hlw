package com.hlw.prescription.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 创建处方草稿请求。
 */
@Getter
@Setter
public class CreatePrescriptionRequest {
    /** 问诊编号。 */
    private Long consultId;
    /** 患者编号。 */
    @NotNull(message = "患者编号不能为空")
    private Long patientId;
    /** 医生编号。 */
    @NotNull(message = "医生编号不能为空")
    private Long doctorId;
    /** 患者姓名。 */
    private String patientName;
    /** 医生姓名。 */
    private String doctorName;
    /** 药品编号列表。 */
    private List<Long> drugIds;
    /** 药品数量。 */
    private Integer drugCount;
    /** 开方时间展示值。 */
    private String issuedAt;
}
