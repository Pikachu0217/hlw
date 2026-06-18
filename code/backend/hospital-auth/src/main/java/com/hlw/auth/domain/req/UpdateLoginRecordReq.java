package com.hlw.auth.domain.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 更新登录记录请求。
 */
@Getter
@Setter
public class UpdateLoginRecordReq {
    /** 登录状态。 */
    @NotBlank(message = "登录状态不能为空")
    private String loginStatus;
    /** 失败原因。 */
    private String failureReason;
    /** 退出时间，格式 yyyy-MM-dd HH:mm:ss。 */
    private String logoutTime;
    /** 客户端 IP。 */
    private String clientIp;
    /** 客户端标识。 */
    private String userAgent;
}
