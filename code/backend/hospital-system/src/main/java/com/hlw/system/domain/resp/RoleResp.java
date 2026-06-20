package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色展示对象。
 */
@Getter
@Setter
public class RoleResp {
    /** 主键编号。 */
    private Long id;
    /** 角色名称。 */
    private String roleName;
    /** 角色编码。 */
    private String roleCode;
    /** 显示顺序。 */
    private Integer orderNum;
    /** 数据范围。 */
    private Integer dataScope;
    /** 成员数量。 */
    private Integer memberCount;
    /** 更新时间。 */
    private String updatedAt;
    /** 状态。 */
    private Integer status;
    /** 是否默认数据（0=系统默认不可删除，1=普通数据可删除）。 */
    private Integer isDefault;
    /** 备注。 */
    private String remark;
}
