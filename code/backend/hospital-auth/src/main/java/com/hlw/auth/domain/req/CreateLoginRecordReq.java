package com.hlw.auth.domain.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 创建登录记录请求。
 */
@Getter
@Setter
public class CreateLoginRecordReq {
    /** 租户编号。 */
    @NotNull(message = "租户编号不能为空")
    private Long tenantId;
    /** 用户编号。 */
    private Long userId;
    /** 登录账号。 */
    @NotBlank(message = "登录账号不能为空")
    private String username;
    /** 用户类型。 */
    private String userType;
    /** 登录状态。 */
    @NotBlank(message = "登录状态不能为空")
    private String loginStatus;
    /** 失败原因。 */
    private String failureReason;
    /** 令牌摘要。 */
    private String tokenDigest;
    /** 客户端 IP。 */
    private String clientIp;
    /** 客户端标识。 */
    private String userAgent;
}
