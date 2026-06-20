package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 租户展示对象。
 */
@Getter
@Setter
public class TenantResp {
    /** 主键编号。 */
    private Long id;
    /** 租户编号。 */
    private String tenantId;
    /** 联系人。 */
    private String contactUserName;
    /** 联系电话。 */
    private String contactPhone;
    /** 企业名称。 */
    private String companyName;
    /** 统一社会信用代码。 */
    private String licenseNumber;
    /** 地址。 */
    private String address;
    /** 企业简介。 */
    private String intro;
    /** 域名。 */
    private String domain;
    /** 备注。 */
    private String remark;
    /** 租户套餐编号。 */
    private Long packageId;
    /** 套餐名称。 */
    private String packageName;
    /** 过期时间。 */
    private String expireTime;
    /** 用户数量限制。 */
    private Integer accountCount;
    /** 租户状态。 */
    private String status;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
}
