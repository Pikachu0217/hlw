package com.hlw.system.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建系统用户请求。
 */
@Getter
@Setter
public class CreateUserRequest {
    /** 登录账号。 */
    @NotBlank(message = "用户账号不能为空")
    private String username;
    /** 联系电话。 */
    private String phone;
    /** 用户类型。 */
    private String userType;
    /** 部门名称。 */
    private String deptName;
    /** 角色名称。 */
    private String roleName;
    /** 账号状态。 */
    private String status;
    /** 登录密码。 */
    private String password;
}
