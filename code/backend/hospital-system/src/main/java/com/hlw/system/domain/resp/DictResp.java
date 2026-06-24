package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 字典展示对象。
 */
@Getter
@Setter
public class DictResp {
    /** 主键编号。 */
    private Long id;
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
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
}
