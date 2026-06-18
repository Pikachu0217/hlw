package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建租户请求。
 */
@Getter
@Setter
public class CreateTenantReq {
    /** 租户名称。 */
    @NotBlank(message = "租户名称不能为空")
    private String tenantName;
    /** 套餐名称。 */
    @NotBlank(message = "套餐名称不能为空")
    private String packageName;
    /** 管理员名称。 */
    @NotBlank(message = "管理员名称不能为空")
    private String adminName;
    /** 到期日期，格式 yyyy-MM-dd。 */
    @NotBlank(message = "到期日期不能为空")
    private String expireAt;
    /** 租户状态。 */
    private String status;
}
