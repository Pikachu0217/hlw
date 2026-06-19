package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 通用关系绑定展示对象。
 */
@Getter
@Setter
public class RelationBindingResp {
    /** 主键编号。 */
    private Long id;
    /** 用户编号。 */
    private String userId;
    /** 角色编号。 */
    private Long roleId;
    /** 菜单编号。 */
    private Long menuId;
}
