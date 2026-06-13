package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统字典持久化对象。
 */
@Getter
@Setter
@TableName("sys_dict")
public class SysDictEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
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
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
    /** 创建人编号。 */
    private Long createBy;
    /** 更新人编号。 */
    private Long updateBy;
    /** 逻辑删除标识。 */
    private Integer deleted;
}
