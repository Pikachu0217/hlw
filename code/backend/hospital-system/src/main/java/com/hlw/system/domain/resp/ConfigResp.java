package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 参数配置展示对象。
 */
@Getter
@Setter
public class ConfigResp {
    /** 表格主键。 */
    private String key;
    /** 配置键。 */
    private String configKey;
    /** 配置值。 */
    private String configValue;
    /** 配置类型。 */
    private String configType;
    /** 状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}
