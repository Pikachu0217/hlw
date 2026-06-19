package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户角色授权展示对象。
 */
@Getter
@Setter
public class UserRoleResp {
    /** 主键编号。 */
    private Long id;
    /** 用户业务编号。 */
    private String userId;
    /** 用户名称。 */
    private String userName;
    /** 角色编号。 */
    private Long roleId;
    /** 角色名称。 */
    private String roleName;
}
