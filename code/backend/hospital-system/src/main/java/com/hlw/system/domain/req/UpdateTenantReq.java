package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新租户请求。
 */
@Getter
@Setter
public class UpdateTenantReq {
    /** 联系人。 */
    private String contactUserName;
    /** 联系电话。 */
    private String contactPhone;
    /** 企业名称。 */
    @NotBlank(message = "企业名称不能为空")
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
    /** 过期时间，格式 yyyy-MM-dd HH:mm:ss。 */
    private String expireTime;
    /** 用户数量限制。 */
    private Integer accountCount;
    /** 租户状态。 */
    @NotBlank(message = "租户状态不能为空")
    private String status;
}
