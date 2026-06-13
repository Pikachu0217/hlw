package com.hlw.system.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 字典展示对象。
 */
@Getter
@Setter
public class DictVO {
    /** 表格主键。 */
    private String key;
    /** 字典类型。 */
    private String dictType;
    /** 字典标签。 */
    private String dictLabel;
    /** 字典键值。 */
    private String dictValue;
    /** 排序。 */
    private Integer sort;
    /** 状态。 */
    private String status;
    /** 备注。 */
    private String remark;
}
