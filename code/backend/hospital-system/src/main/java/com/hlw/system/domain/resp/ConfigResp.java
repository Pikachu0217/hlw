package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 参数配置展示对象。
 */
@Getter
@Setter
public class ConfigResp {
    /** 主键编号。 */
    private Long id;
    /** 参数名称。 */
    private String configName;
    /** 参数键名。 */
    private String configKey;
    /** 参数键值。 */
    private String configValue;
    /** 备注。 */
    private String remark;
}
