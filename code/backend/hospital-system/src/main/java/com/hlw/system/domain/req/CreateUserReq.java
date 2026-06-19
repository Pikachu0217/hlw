package com.hlw.system.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建系统用户请求。
 */
@Getter
@Setter
public class CreateUserReq {
    /** 登录账号。 */
    @NotBlank(message = "用户账号不能为空")
    private String userName;
    /** 用户昵称。 */
    private String nickName;
    /** 部门编号。 */
    private Long deptId;
    /** 用户类型。 */
    private String userType;
    /** 用户邮箱。 */
    private String email;
    /** 联系电话。 */
    private String phone;
    /** 用户性别。 */
    private String sex;
    /** 账号状态。 */
    private Integer status;
    /** 登录密码。 */
    private String password;
    /** 备注。 */
    private String remark;
}
