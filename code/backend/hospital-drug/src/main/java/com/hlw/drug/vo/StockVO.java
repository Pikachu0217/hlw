package com.hlw.drug.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 药品库存展示对象。
 */
@Getter
@Setter
public class StockVO {    /** 库存编号。 */
    private Long id;
    /** 药品名称。 */
    private String drugName;
    /** 仓库名称。 */
    private String warehouseName;
    /** 库存数量。 */
    private Integer inventory;
    /** 预警状态。 */
    private String warningStatus;
}
