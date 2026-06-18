package com.hlw.auth.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 登录记录展示对象。
 */
@Getter
@Setter
public class LoginRecordResp {
    /** 表格主键。 */
    private String key;
    /** 租户编号。 */
    private Long tenantId;
    /** 用户编号。 */
    private Long userId;
    /** 登录账号。 */
    private String username;
    /** 用户类型。 */
    private String userType;
    /** 登录状态。 */
    private String loginStatus;
    /** 失败原因。 */
    private String failureReason;
    /** 令牌摘要。 */
    private String tokenDigest;
    /** 登录时间。 */
    private String loginTime;
    /** 退出时间。 */
    private String logoutTime;
    /** 客户端 IP。 */
    private String clientIp;
    /** 客户端标识。 */
    private String userAgent;
}
