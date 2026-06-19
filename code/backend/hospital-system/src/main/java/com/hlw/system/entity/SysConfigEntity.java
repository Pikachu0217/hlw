package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统参数配置持久化对象。
 */
@Getter
@Setter
@TableName("sys_config")
public class SysConfigEntity extends SystemBaseEntity {
    /** 参数名称。 */
    private String configName;
    /** 参数键名。 */
    private String configKey;
    /** 参数键值。 */
    private String configValue;
    /** 备注。 */
    private String remark;
}
