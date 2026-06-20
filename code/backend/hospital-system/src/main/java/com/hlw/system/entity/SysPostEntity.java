package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统岗位持久化对象。
 */
@Getter
@Setter
@TableName("sys_post")
public class SysPostEntity extends BaseEntity {
    /** 岗位编码。 */
    private String postCode;
    /** 岗位名称。 */
    private String postName;
    /** 显示排序。 */
    private Integer orderNum;
    /** 备注。 */
    private String remark;
    /** 岗位状态。 */
    private Integer status;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
}
