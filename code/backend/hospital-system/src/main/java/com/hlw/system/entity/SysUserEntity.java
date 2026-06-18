package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统用户持久化对象。
 */
@Getter
@Setter
@TableName("sys_user")
public class SysUserEntity extends BaseEntity {
    /** 用户名称。 */
    private String username;
    /** 登录密码。 */
    private String password;
    /** 联系电话。 */
    private String phone;
    /** 用户类型。 */
    private String userType;
    /** 部门编号。 */
    private Long deptId;
    /** 部门名称。 */
    private String deptName;
    /** 角色名称。 */
    private String roleName;
    /** 最近登录时间描述。 */
    private String lastLogin;
    /** 账号状态。 */
    private String status;
}
