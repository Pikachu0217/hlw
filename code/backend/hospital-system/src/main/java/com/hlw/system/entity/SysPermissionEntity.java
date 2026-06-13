package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统权限码持久化对象。
 */
@Getter
@Setter
@TableName("sys_permission")
public class SysPermissionEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 权限名称。 */
    private String permissionName;
    /** 权限编码。 */
    private String permissionCode;
    /** 资源类型。 */
    private String resourceType;
    /** 关联菜单编号。 */
    private Long menuId;
    /** 权限状态。 */
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
