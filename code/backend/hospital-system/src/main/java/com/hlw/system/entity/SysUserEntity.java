package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统用户持久化对象。
 */
@Getter
@Setter
@TableName("sys_user")
public class SysUserEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 用户名称。 */
    private String username;
    /** 登录密码。 */
    private String password;
    /** 联系电话。 */
    private String phone;
    /** 用户类型。 */
    private String userType;
    /** 部门名称。 */
    private String deptName;
    /** 角色名称。 */
    private String roleName;
    /** 最近登录时间描述。 */
    private String lastLogin;
    /** 账号状态。 */
    private String status;
    /** 创建时间。 */
    private LocalDateTime createTime;
    /** 更新时间。 */
    private LocalDateTime updateTime;
    /** 创建人编号。 */
    private Long createBy;
    /** 更新人编号。 */
    private Long updateBy;
    /** 逻辑删除标识。 */
    private Integer deleted;
}
