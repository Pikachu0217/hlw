package com.hlw.system.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 菜单展示对象。
 */
@Getter
@Setter
public class MenuVO {
    /** 表格主键。 */
    private String key;
    /** 父级菜单编号。 */
    private String parentId;
    /** 菜单名称。 */
    private String menuName;
    /** 菜单类型。 */
    private String menuType;
    /** 权限标识。 */
    private String permission;
    /** 路由路径。 */
    private String routePath;
    /** 排序。 */
    private Integer sort;
    /** 状态。 */
    private String status;
}
