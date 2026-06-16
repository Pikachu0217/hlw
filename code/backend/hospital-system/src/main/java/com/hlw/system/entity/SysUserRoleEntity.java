package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户角色关系持久化对象。
 */
@Getter
@Setter
@TableName("sys_user_role")
public class SysUserRoleEntity extends BaseEntity {
    /** 用户编号。 */
    private Long userId;
    /** 角色编号。 */
    private Long roleId;
    /** 关联状态。 */
    private String status;
}
