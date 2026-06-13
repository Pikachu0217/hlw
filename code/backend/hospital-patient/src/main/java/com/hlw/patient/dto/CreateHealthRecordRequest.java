package com.hlw.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建健康档案请求。
 */
@Getter
@Setter
public class CreateHealthRecordRequest {
    /** 患者编号。 */
    @NotNull(message = "患者编号不能为空")
    private Long patientId;
    /** 档案标题。 */
    @NotBlank(message = "档案标题不能为空")
    private String title;
    /** 档案摘要。 */
    @NotBlank(message = "档案摘要不能为空")
    private String summary;
    /** 过敏史。 */
    private String allergies;
    /** 既往病史。 */
    private String history;
    /** 诊断信息。 */
    private String diagnosis;
    /** 备注。 */
    private String remark;
}
