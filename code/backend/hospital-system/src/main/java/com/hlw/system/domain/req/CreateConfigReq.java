package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建系统参数请求。
 */
@Getter
@Setter
public class CreateConfigReq {
    /** 配置键。 */
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    /** 配置值。 */
    @NotBlank(message = "配置值不能为空")
    private String configValue;
    /** 配置类型。 */
    private String configType;
    /** 配置状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}
