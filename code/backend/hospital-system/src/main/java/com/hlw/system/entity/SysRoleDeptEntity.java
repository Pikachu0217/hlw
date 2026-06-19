package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色部门关系持久化对象。
 */
@Getter
@Setter
@TableName("sys_role_dept")
public class SysRoleDeptEntity extends BaseEntity {
    /** 角色编号。 */
    private Long roleId;
    /** 部门编号。 */
    private Long deptId;
}
