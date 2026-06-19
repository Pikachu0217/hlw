package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色部门关系持久化对象。
 */
@Getter
@Setter
@TableName("sys_role_dept")
public class SysRoleDeptEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private String tenantId;
    /** 角色编号。 */
    private Long roleId;
    /** 部门编号。 */
    private Long deptId;
}
