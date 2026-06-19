package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户角色授权展示对象。
 */
@Getter
@Setter
public class UserRoleResp {
    /** 表格主键。 */
    private String key;
    /** 用户业务编号。 */
    private String userId;
    /** 用户名称。 */
    private String userName;
    /** 角色名称。 */
    private String roleName;
}
