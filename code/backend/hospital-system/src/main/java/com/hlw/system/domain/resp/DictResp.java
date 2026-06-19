package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 字典展示对象。
 */
@Getter
@Setter
public class DictResp {
    /** 表格主键。 */
    private String key;
    /** 字典名称。 */
    private String dictName;
    /** 字典类型。 */
    private String dictType;
    /** 字典标签。 */
    private String dictLabel;
    /** 字典键值。 */
    private String dictValue;
    /** 字典排序。 */
    private Integer dictSort;
    /** 备注。 */
    private String remark;
}
