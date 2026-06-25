package com.hlw.drug.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 药品信息持久化对象。
 */
@Getter
@Setter
@TableName("drug_info")
public class DrugInfoEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 兼容旧表药品名称。 */
    private String name;
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
