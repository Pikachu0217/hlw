package com.hlw.system.domain.resp;

import lombok.Getter;
import lombok.Setter;

/**
 * 登录日志展示对象。
 */
@Getter
@Setter
public class LoginInfoResp {
    /** 表格主键。 */
    private String key;
    /** 租户编号。 */
    private String tenantId;
    /** 用户账号。 */
    private String userName;
    /** 客户端。 */
    private String clientKey;
    /** 设备类型。 */
    private String deviceType;
    /** 登录IP地址。 */
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
    /** 访问时间。 */
    private String loginTime;
}
