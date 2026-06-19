package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统角色持久化对象。
 */
@Getter
@Setter
@TableName("sys_role")
public class SysRoleEntity extends SystemBaseEntity {
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
    /** 备注。 */
    private String remark;
}
