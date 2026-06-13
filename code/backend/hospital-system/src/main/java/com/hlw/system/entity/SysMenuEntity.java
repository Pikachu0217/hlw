package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统菜单持久化对象。
 */
@Getter
@Setter
@TableName("sys_menu")
public class SysMenuEntity {
    /** 主键编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 菜单名称。 */
    private String menuName;
    /** 菜单类型。 */
    private String menuType;
    /** 权限标识。 */
    private String permission;
    /** 路由路径。 */
    private String routePath;
    /** 菜单状态。 */
    private String status;
    /** 父级菜单编号。 */
    private Long parentId;
    /** 菜单排序。 */
    private Integer sort;
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
