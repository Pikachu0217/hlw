package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统菜单持久化对象。
 */
@Getter
@Setter
@TableName("sys_menu")
public class SysMenuEntity extends BaseEntity {
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
}
