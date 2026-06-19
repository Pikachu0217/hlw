package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 租户信息持久化对象。
 */
@Getter
@Setter
@TableName("sys_tenant")
public class SysTenantEntity extends SystemBaseEntity {
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
    /** 过期时间。 */
    private LocalDateTime expireTime;
    /** 用户数量限制。 */
    private Integer accountCount;
    /** 租户状态。 */
    private String status;
}
