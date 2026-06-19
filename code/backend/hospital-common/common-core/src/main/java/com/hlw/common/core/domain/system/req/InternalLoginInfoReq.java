package com.hlw.common.core.domain.system.req;

import lombok.Getter;
import lombok.Setter;

/**
 * hospital-system 内部登录日志写入请求。
 */
@Getter
@Setter
public class InternalLoginInfoReq {
    /** 租户编号。 */
    private Long tenantId;
    /** 用户账号。 */
    private String userName;
    /** 客户端标识。 */
    private String clientKey;
    /** 设备类型。 */
    private String deviceType;
    /** 登录 IP 地址。 */
    private String ipaddr;
    /** 登录地点。 */
    private String loginLocation;
    /** 浏览器类型。 */
    private String browser;
    /** 操作系统。 */
    private String os;
    /** 登录状态。 */
    private Integer status;
    /** 提示消息。 */
    private String msg;
}
