package com.hlw.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新系统参数请求。
 */
@Getter
@Setter
public class UpdateConfigRequest {
    /** 配置值。 */
    @NotBlank(message = "配置值不能为空")
    private String configValue;
    /** 备注。 */
    private String remark;
}
