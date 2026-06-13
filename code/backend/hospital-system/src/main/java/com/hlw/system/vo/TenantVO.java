package com.hlw.system.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 租户展示对象。
 */
@Getter
@Setter
public class TenantVO {
    /** 表格主键。 */
    private String key;
    /** 租户编号。 */
    private Long tenantId;
    /** 租户名称。 */
    private String tenantName;
    /** 套餐名称。 */
    private String packageName;
    /** 管理员名称。 */
    private String adminName;
    /** 到期日期。 */
    private String expireAt;
    /** 租户状态。 */
    private String status;
}
