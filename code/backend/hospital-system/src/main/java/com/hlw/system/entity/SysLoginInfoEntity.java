package com.hlw.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 系统访问记录持久化对象。
 */
@Getter
@Setter
@TableName("sys_login_info")
public class SysLoginInfoEntity {
    /** 访问编号。 */
    @TableId(type = IdType.AUTO)
    private Long id;
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
    private LocalDateTime loginTime;
}
