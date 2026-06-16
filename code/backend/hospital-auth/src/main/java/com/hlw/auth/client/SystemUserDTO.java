package com.hlw.auth.client;

import lombok.Getter;
import lombok.Setter;

/**
 * 从 hospital-system 内部接口接收的用户数据传输对象。
 */
@Getter
@Setter
public class SystemUserDTO {
    /** 用户编号。 */
    private Long id;
    /** 租户编号。 */
    private Long tenantId;
    /** 登录账号。 */
    private String username;
    /** 登录密码哈希。 */
    private String password;
    /** 联系电话。 */
    private String phone;
    /** 用户类型。 */
    private String userType;
    /** 账号状态。 */
    private String status;
}
