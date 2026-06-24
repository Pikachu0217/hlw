package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典类型持久化对象。
 */
@Getter
@Setter
@TableName("sys_dict_type")
public class SysDictTypeEntity extends BaseEntity {
    /** 字典名称。 */
    private String dictName;
    /** 字典类型。 */
    private String dictType;
    /** 备注。 */
    private String remark;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
}
