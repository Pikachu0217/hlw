package com.hlw.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建权限码请求。
 */
@Getter
@Setter
public class CreatePermissionRequest {
    /** 权限名称。 */
    @NotBlank(message = "权限名称不能为空")
    private String permissionName;
    /** 权限编码。 */
    @NotBlank(message = "权限编码不能为空")
    private String permissionCode;
    /** 资源类型。 */
    private String resourceType;
    /** 菜单编号。 */
    private Long menuId;
    /** 状态。 */
    private String status;
}
