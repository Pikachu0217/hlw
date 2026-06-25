package com.hlw.consult.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 创建问诊请求。
 */
@Getter
@Setter
public class CreateConsultRequest {
    /** 患者编号。 */
    private Long patientId;
    /** 医生编号。 */
    private Long doctorId;
    /** 问诊类型。 */
    private String type;
    /** 患者姓名。 */
    private String patientName;
    /** 医生姓名。 */
    private String doctorName;
    /** 问诊渠道。 */
    private String channel;
    /** 问题描述。 */
    @NotBlank(message = "问题描述不能为空")
    private String chiefComplaint;
    /** 问诊费用。 */
    @DecimalMin(value = "0", message = "问诊费用不能小于 0")
    private BigDecimal feeAmount;
}
