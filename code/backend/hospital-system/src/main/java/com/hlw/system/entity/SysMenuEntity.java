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
    /** 父级菜单编号。 */
    private Long parentId;
    /** 显示顺序。 */
    private Integer orderNum;
    /** 路由地址。 */
    private String path;
    /** 组件路径。 */
    private String component;
    /** 是否外链。 */
    private Integer isFrame;
    /** 菜单类型。 */
    private String menuType;
    /** 显示状态。 */
    private String visible;
    /** 菜单状态。 */
    private String status;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
    /** 权限标识。 */
    private String perms;
    /** 菜单图标。 */
    private String icon;
    /** 平台模板菜单编号。 */
    private Long sourceMenuId;
    /** 备注。 */
    private String remark;
}
