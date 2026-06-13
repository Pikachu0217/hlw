package com.hlw.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 创建医生请求。
 */
@Getter
@Setter
public class CreateDoctorRequest {
    /** 医生姓名。 */
    @NotBlank(message = "医生姓名不能为空")
    private String name;
    /** 医生职称。 */
    @NotBlank(message = "医生职称不能为空")
    private String title;
    /** 所属科室。 */
    @NotBlank(message = "所属科室不能为空")
    private String department;
    /** 擅长方向。 */
    private String specialty;
    /** 问诊费用。 */
    private BigDecimal consultFee;
    /** 接诊状态。 */
    private String consultStatus;
    /** 展示状态。 */
    private String status;
    /** 排班描述。 */
    private String schedule;
    /** 关联用户编号。 */
    private Long userId;
}
