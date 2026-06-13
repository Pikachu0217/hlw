package com.hlw.system.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 通用关系绑定展示对象。
 */
@Getter
@Setter
public class RelationBindingVO {
    /** 表格主键。 */
    private String key;
    /** 用户编号。 */
    private Long userId;
    /** 角色编号。 */
    private Long roleId;
    /** 菜单编号。 */
    private Long menuId;
    /** 关联状态。 */
    private String status;
}
