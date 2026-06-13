package com.hlw.auth.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 登录用户资料展示对象。
 */
@Getter
@Setter
public class UserProfileVO {
    /** 表格主键。 */
    private String key;
    /** 用户编号。 */
    private Long userId;
    /** 租户编号。 */
    private Long tenantId;
    /** 登录账号。 */
    private String username;
    /** 联系电话。 */
    private String phone;
    /** 用户类型。 */
    private String userType;
    /** 角色名称。 */
    private String roleName;
    /** 账号状态。 */
    private String status;
}
