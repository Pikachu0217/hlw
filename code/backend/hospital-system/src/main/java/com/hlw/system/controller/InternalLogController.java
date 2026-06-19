package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.domain.system.req.InternalLoginInfoReq;
import com.hlw.system.service.LoginInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统内部日志接口，供内部服务写入系统日志表。
 */
@RestController
@RequestMapping("/internal/log")
@RequiredArgsConstructor
@Slf4j
public class InternalLogController {
    /** 登录日志服务。 */
    private final LoginInfoService loginInfoService;

    /**
     * 记录登录日志。
     *
     * @param request 登录日志写入请求
     * @return 空响应
     */
    @PostMapping("/login")
    public R<Void> recordLoginInfo(@RequestBody InternalLoginInfoReq request) {
        log.info("接收内部登录日志写入请求，tenantId={}，userName={}，status={}",
            request == null ? null : request.getTenantId(),
            request == null ? null : request.getUserName(),
            request == null ? null : request.getStatus());
        loginInfoService.recordLoginInfo(request);
        return R.ok(null);
    }
}
