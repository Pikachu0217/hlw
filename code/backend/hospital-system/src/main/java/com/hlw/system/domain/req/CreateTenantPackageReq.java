package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 创建租户套餐请求。
 */
@Getter
@Setter
public class CreateTenantPackageReq {
    /** 套餐名称。 */
    @NotBlank(message = "套餐名称不能为空")
    private String packageName;
    /** 备注。 */
    private String remark;
    /** 状态。 */
    private Integer status;
    /** 菜单编号列表。 */
    private List<Long> menuIds;
}
