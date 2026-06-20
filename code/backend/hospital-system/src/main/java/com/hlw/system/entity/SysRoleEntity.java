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
    /** 显示顺序。 */
    private Integer orderNum;
    /** 数据权限范围。 */
    private Integer dataScope;
    /** 角色状态。 */
    private Integer status;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
    /** 备注。 */
    private String remark;
}
