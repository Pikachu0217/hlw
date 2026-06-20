package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统用户持久化对象。
 */
@Getter
@Setter
@TableName("sys_user")
public class SysUserEntity extends BaseEntity {
    /** 用户业务编号。 */
    private String userId;
    /** 部门编号。 */
    private Long deptId;
    /** 登录账号。 */
    private String userName;
    /** 用户昵称。 */
    private String nickName;
    /** 用户类型。 */
    private String userType;
    /** 用户邮箱。 */
    private String email;
    /** 联系电话。 */
    private String phone;
    /** 用户性别。 */
    private String sex;
    /** 头像地址。 */
    private Long avatar;
    /** 登录密码。 */
    private String password;
    /** 账号状态。 */
    private Integer status;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
    /** 最后登录IP。 */
    private String loginIp;
    /** 最后登录时间。 */
    private LocalDateTime loginDate;
    /** 备注。 */
    private String remark;
}
