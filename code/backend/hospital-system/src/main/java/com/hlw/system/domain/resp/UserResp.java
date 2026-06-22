package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户展示对象。
 */
@Getter
@Setter
public class UserResp {
    /** 主键编号。 */
    private Long id;
    /** 用户业务编号。 */
    private String userId;
    /** 登录账号。 */
    private String userName;
    /** 真实姓名。 */
    private String realName;
    /** 用户昵称。 */
    private String nickName;
    /** 部门编号。 */
    private Long deptId;
    /** 部门名称。 */
    private String deptName;
    /** 角色名称。 */
    private String roleName;
    /** 岗位名称。 */
    private String postName;
    /** 联系电话。 */
    private String phone;
    /** 用户邮箱。 */
    private String email;
    /** 用户类型。 */
    private String userType;
    /** 最近登录时间。 */
    private String lastLogin;
    /** 状态。 */
    private Integer status;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
}
