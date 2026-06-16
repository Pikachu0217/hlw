package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统字典持久化对象。
 */
@Getter
@Setter
@TableName("sys_dict")
public class SysDictEntity extends BaseEntity {
    /** 字典类型。 */
    private String dictType;
    /** 字典标签。 */
    private String dictLabel;
    /** 字典键值。 */
    private String dictValue;
    /** 字典状态。 */
    private String status;
    /** 显示排序。 */
    private Integer sort;
    /** 备注。 */
    private String remark;
}
