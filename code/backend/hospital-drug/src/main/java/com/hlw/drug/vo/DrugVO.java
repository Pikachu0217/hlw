package com.hlw.drug.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 药品资料展示对象。
 */
@Getter
@Setter
public class DrugVO {    /** 药品编号。 */
    private Long id;
    /** 药品名称。 */
    private String drugName;
    /** 药品规格。 */
    private String spec;
    /** 库存数量。 */
    private Integer inventory;
    /** 库存单位。 */
    private String unit;
    /** 预警状态。 */
    private String warningStatus;
}
