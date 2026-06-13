package com.hlw.drug.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 配送发货展示对象。
 */
@Getter
@Setter
public class DeliveryShipVO {
    /** 配送单编号。 */
    private Long id;
    /** 配送状态。 */
    private String status;
}
