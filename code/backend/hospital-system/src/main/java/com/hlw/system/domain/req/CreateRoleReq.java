package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建角色请求。
 */
@Getter
@Setter
public class CreateRoleReq {
    /** 角色名称。 */
    @NotBlank(message = "角色名称不能为空")
    private String roleName;
    /** 角色编码。 */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;
    /** 显示顺序。 */
    private Integer orderNum;
    /** 数据范围。 */
    private Integer dataScope;
    /** 角色状态。 */
    private Integer status;
    /** 备注。 */
    private String remark;
}
