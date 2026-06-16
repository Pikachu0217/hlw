package com.hlw.system.vo;

import lombok.Getter;
import lombok.Setter;

/**
 * 内部服务间用户展示对象，仅供 hospital-auth 通过 OpenFeign 调用使用，不允许外部网关暴露。
 */
@Getter
@Setter
public class InternalUserVO {
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
