package com.hlw.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建角色请求。
 */
@Getter
@Setter
public class CreateRoleRequest {
    /** 角色名称。 */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    /** 角色编码。 */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    /** 数据范围。 */
    private String dataScope;
    /** 角色状态。 */
    private String status;
}
