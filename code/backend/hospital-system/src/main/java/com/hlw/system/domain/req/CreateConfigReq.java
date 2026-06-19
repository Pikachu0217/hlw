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
    /** 参数名称。 */
    @NotBlank(message = "参数名称不能为空")
    private String configName;
    /** 参数键名。 */
    @NotBlank(message = "配置键不能为空")
    private String configKey;
    /** 参数键值。 */
    @NotBlank(message = "配置值不能为空")
    private String configValue;
    /** 备注。 */
    private String remark;
}
