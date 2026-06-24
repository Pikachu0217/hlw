package com.hlw.patient.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建患者请求。
 */
@Getter
@Setter
public class CreatePatientRequest {
    /** 关联用户编号（关联 sys_user.user_id 字符串）。 */
    @NotBlank(message = "关联用户编号不能为空")
    private String userId;
    /** 患者姓名。 */
    @NotBlank(message = "患者姓名不能为空")
    private String patientName;
    /** 患者性别。 */
    @NotBlank(message = "患者性别不能为空")
    private String gender;
    /** 患者年龄。 */
    @NotNull(message = "患者年龄不能为空")
    @Min(value = 0, message = "患者年龄不能小于 0")
    private Integer age;
    /** 联系电话。 */
    @NotBlank(message = "联系电话不能为空")
    private String phone;
    /** 风险等级。 */
    private String riskLevel;
    /** 身份证号。 */
    private String idCard;
    /** 出生日期。 */
    private String birthday;
    /** 联系地址。 */
    private String address;
    /** 最近就诊日期。 */
    private String lastVisit;
}
