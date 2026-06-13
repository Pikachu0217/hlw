package com.hlw.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新医生状态请求。
 */
@Getter
@Setter
public class UpdateDoctorStatusRequest {
    /** 医生状态。 */
    @NotBlank(message = "医生状态不能为空")
    private String status;
}
