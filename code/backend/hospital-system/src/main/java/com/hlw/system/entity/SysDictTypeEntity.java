package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 字典类型持久化对象。
 */
@Getter
@Setter
@TableName("sys_dict_type")
public class SysDictTypeEntity extends SystemBaseEntity {
    /** 字典名称。 */
    private String dictName;
    /** 字典类型。 */
    private String dictType;
    /** 备注。 */
    private String remark;
}
