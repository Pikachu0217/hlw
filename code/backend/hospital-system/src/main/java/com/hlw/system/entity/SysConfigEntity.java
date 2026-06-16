package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统参数配置持久化对象。
 */
@Getter
@Setter
@TableName("sys_config")
public class SysConfigEntity extends BaseEntity {
    /** 配置键。 */
    private String configKey;
    /** 配置值。 */
    private String configValue;
    /** 配置类型。 */
    private String configType;
    /** 配置状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}
