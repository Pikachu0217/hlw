package com.hlw.auth.service;

import com.hlw.auth.client.SystemLogFeignClient;
import com.hlw.auth.domain.resp.LoginUserResp;
import com.hlw.common.core.domain.system.req.InternalLoginInfoReq;
import com.hlw.common.core.util.DefaultValueUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 登录审计服务，负责把认证事件转发到 hospital-system 的系统登录日志表。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoginAuditService {
    /** 系统登录日志成功状态。 */
    private static final int SYSTEM_LOGIN_SUCCESS = 0;
    /** 系统登录日志失败状态。 */
    private static final int SYSTEM_LOGIN_FAILURE = 1;
    /** 系统日志 Feign 客户端。 */
    private final SystemLogFeignClient systemLogFeignClient;

    /**
     * 记录登录成功。
     *
     * @param user 登录用户信息
     * @param rawToken 原始令牌
     * @param clientIp 客户端 IP
     * @param userAgent 客户端标识
     */
    public void recordLoginSuccess(LoginUserResp user, String rawToken, String clientIp, String userAgent) {
        log.info("转发登录成功日志，tenantId={}，userId={}，username={}，tokenPresent={}",
            user.tenantId(), user.id(), user.username(), StringUtils.hasText(rawToken));
        recordSystemLoginInfo(buildSystemLoginInfo(
            user.tenantId(), user.username(), SYSTEM_LOGIN_SUCCESS, "登录成功", clientIp, userAgent));
    }

    /**
     * 记录登录失败。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @param failureReason 失败原因
     * @param clientIp 客户端 IP
     * @param userAgent 客户端标识
     */
    public void recordLoginFailure(Long tenantId, String username, String failureReason, String clientIp, String userAgent) {
        log.warn("转发登录失败日志，tenantId={}，username={}，failureReason={}", tenantId, username, failureReason);
        recordSystemLoginInfo(buildSystemLoginInfo(
            tenantId, username, SYSTEM_LOGIN_FAILURE, failureReason, clientIp, userAgent));
    }

    /**
     * 记录退出登录。
     *
     * @param rawToken 原始令牌
     */
    public void recordLogout(String rawToken) {
        log.info("认证服务无库化后不再回写退出登录记录，tokenPresent={}", StringUtils.hasText(rawToken));
    }

    /**
     * 写入系统模块登录日志。
     *
     * @param request 系统登录日志写入请求
     */
    private void recordSystemLoginInfo(InternalLoginInfoReq request) {
        try {
            systemLogFeignClient.recordLoginInfo(request);
        } catch (RuntimeException exception) {
            log.warn("写入系统登录日志失败，tenantId={}，username={}，message={}",
                request.getTenantId(), request.getUserName(), exception.getMessage());
        }
    }

    /**
     * 构造系统登录日志写入请求。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @param status 登录状态
     * @param msg 提示消息
     * @param clientIp 客户端 IP
     * @param userAgent 客户端标识
     * @return 系统登录日志写入请求
     */
    private InternalLoginInfoReq buildSystemLoginInfo(
            Long tenantId,
            String username,
            Integer status,
            String msg,
            String clientIp,
            String userAgent
    ) {
        InternalLoginInfoReq request = new InternalLoginInfoReq();
        request.setTenantId(tenantId);
        request.setUserName(truncate(DefaultValueUtils.defaultIfBlank(username, ""), 50));
        request.setClientKey("admin-web");
        request.setDeviceType(resolveDeviceType(userAgent));
        request.setIpaddr(truncate(DefaultValueUtils.defaultIfBlank(clientIp, ""), 128));
        request.setLoginLocation("");
        request.setBrowser(resolveBrowser(userAgent));
        request.setOs(resolveOs(userAgent));
        request.setStatus(status);
        request.setMsg(truncate(DefaultValueUtils.defaultIfBlank(msg, ""), 255));
        return request;
    }

    /**
     * 解析设备类型。
     *
     * @param userAgent 客户端标识
     * @return 设备类型
     */
    private String resolveDeviceType(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "UNKNOWN";
        }
        return userAgent.toLowerCase().contains("mobile") ? "MOBILE" : "PC";
    }

    /**
     * 解析浏览器类型。
     *
     * @param userAgent 客户端标识
     * @return 浏览器类型
     */
    private String resolveBrowser(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "UNKNOWN";
        }
        String lowerUserAgent = userAgent.toLowerCase();
        if (lowerUserAgent.contains("edg")) {
            return "Edge";
        }
        if (lowerUserAgent.contains("chrome")) {
            return "Chrome";
        }
        if (lowerUserAgent.contains("safari")) {
            return "Safari";
        }
        if (lowerUserAgent.contains("firefox")) {
            return "Firefox";
        }
        return "Other";
    }

    /**
     * 解析操作系统。
     *
     * @param userAgent 客户端标识
     * @return 操作系统
     */
    private String resolveOs(String userAgent) {
        if (!StringUtils.hasText(userAgent)) {
            return "UNKNOWN";
        }
        String lowerUserAgent = userAgent.toLowerCase();
        if (lowerUserAgent.contains("windows")) {
            return "Windows";
        }
        if (lowerUserAgent.contains("mac os") || lowerUserAgent.contains("macintosh")) {
            return "macOS";
        }
        if (lowerUserAgent.contains("android")) {
            return "Android";
        }
        if (lowerUserAgent.contains("iphone") || lowerUserAgent.contains("ipad")) {
            return "iOS";
        }
        if (lowerUserAgent.contains("linux")) {
            return "Linux";
        }
        return "Other";
    }

    /**
     * 截断字符串。
     *
     * @param value 原始字符串
     * @param maxLength 最大长度
     * @return 截断后字符串
     */
    private String truncate(String value, int maxLength) {
        if (value == null) {
            return "";
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}
