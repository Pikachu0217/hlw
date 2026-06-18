package com.hlw.auth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hlw.common.core.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 认证登录记录持久化对象。
 */
@Getter
@Setter
@TableName("auth_login_record")
public class AuthLoginRecordEntity extends BaseEntity {
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
    private LocalDateTime loginTime;
    /** 退出时间。 */
    private LocalDateTime logoutTime;
    /** 客户端 IP。 */
    private String clientIp;
    /** 客户端标识。 */
    private String userAgent;
}
