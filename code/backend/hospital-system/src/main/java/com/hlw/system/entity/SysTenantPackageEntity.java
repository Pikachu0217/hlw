package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 租户套餐持久化对象。
 */
@Getter
@Setter
@TableName("sys_tenant_package")
public class SysTenantPackageEntity extends SystemAuditEntity {
    /** 套餐名称。 */
    private String packageName;
    /** 备注。 */
    private String remark;
    /** 状态。 */
    private Integer status;
}
