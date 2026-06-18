package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 角色展示对象。
 */
@Getter
@Setter
public class RoleResp {
    /** 表格主键。 */
    private String key;
    /** 角色名称。 */
    private String roleName;
    /** 角色编码。 */
    private String roleCode;
    /** 数据范围。 */
    private String dataScope;
    /** 成员数量。 */
    private Integer memberCount;
    /** 更新时间。 */
    private String updatedAt;
    /** 状态。 */
    private String status;
}
