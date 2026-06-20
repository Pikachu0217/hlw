package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 菜单展示对象。
 */
@Getter
@Setter
public class MenuResp {
    /** 主键编号。 */
    private Long id;
    /** 父级菜单编号。 */
    private String parentId;
    /** 菜单名称。 */
    private String menuName;
    /** 菜单类型。 */
    private String menuType;
    /** 权限标识。 */
    private String perms;
    /** 路由路径。 */
    private String path;
    /** 组件路径。 */
    private String component;
    /** 是否外链。 */
    private Integer isFrame;
    /** 显示状态。 */
    private String visible;
    /** 排序。 */
    private Integer orderNum;
    /** 图标。 */
    private String icon;
    /** 备注。 */
    private String remark;
    /** 状态。 */
    private String status;
    /** 子菜单列表。 */
    private List<MenuResp> children;
}
