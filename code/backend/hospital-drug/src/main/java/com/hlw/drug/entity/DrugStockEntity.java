package com.hlw.drug.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 药品库存持久化对象。
 */
@Getter
@Setter
@TableName("drug_stock")
public class DrugStockEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 药品编号。 */
    private Long drugId;
    /** 仓库名称。 */
    private String warehouseName;
    /** 库存数量。 */
    private Integer inventory;
    /** 预警状态。 */
    private String warningStatus;
    /** 库存数量兼容字段。 */
    private BigDecimal stockQty;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
    /** 创建人编号。 */
    private Long createBy;
    /** 更新人编号。 */
    private Long updateBy;
    /** 逻辑删除标识。 */
    @TableLogic
    private Integer deleted;
}
