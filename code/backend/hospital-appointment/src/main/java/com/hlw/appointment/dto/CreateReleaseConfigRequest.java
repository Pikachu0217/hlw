package com.hlw.appointment.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建放号配置请求。
 */
@Getter
@Setter
public class CreateReleaseConfigRequest {
    /** 排班编号。 */
    @NotNull(message = "排班编号不能为空")
    private Long scheduleId;
    /** 放号时间。 */
    @NotBlank(message = "放号时间不能为空")
    private String releaseAt;
    /** 放号数量。 */
    @Min(value = 1, message = "放号数量必须大于 0")
    private Integer releaseCount;
    /** 配置状态。 */
    private String status;
}
