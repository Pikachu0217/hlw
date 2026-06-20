package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 租户套餐持久化对象。
 */
@Getter
@Setter
@TableName("sys_tenant_package")
public class SysTenantPackageEntity extends BaseEntity {
    /** 套餐名称。 */
    private String packageName;
    /** 备注。 */
    private String remark;
    /** 状态。 */
    private Integer status;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
}
