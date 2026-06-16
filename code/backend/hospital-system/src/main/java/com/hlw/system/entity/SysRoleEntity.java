package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统角色持久化对象。
 */
@Getter
@Setter
@TableName("sys_role")
public class SysRoleEntity extends BaseEntity {
    /** 角色名称。 */
    private String roleName;
    /** 角色编码。 */
    private String roleCode;
    /** 数据权限范围。 */
    private String dataScope;
    /** 成员数量。 */
    private Integer memberCount;
    /** 角色状态。 */
    private String status;
}
