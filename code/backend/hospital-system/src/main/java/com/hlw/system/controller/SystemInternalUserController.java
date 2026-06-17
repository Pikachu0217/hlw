package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.domain.system.resp.InternalUserResp;
import com.hlw.system.service.InternalUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

/**
 * 系统内部用户接口，专供 hospital-auth 通过 OpenFeign 服务间直连调用，
 * 网关不应对 /internal/** 进行外部路由。
 */
@RestController
@RequestMapping("/internal/system")
@Slf4j
public class SystemInternalUserController {
    private final InternalUserService internalUserService;

    /**
     * 构造系统内部用户控制器。
     *
     * @param internalUserService 内部用户查询服务
     */
    public SystemInternalUserController(InternalUserService internalUserService) {
        this.internalUserService = internalUserService;
    }

    /**
     * 按租户编号和登录账号查询用户。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @return 内部用户展示对象，不存在返回 data=null
     */
    @GetMapping("/users")
    public R<InternalUserResp> users(@RequestParam Long tenantId, @RequestParam String username) {
        log.info("接收内部用户查询请求，tenantId={}，username={}", tenantId, username);
        return R.ok(internalUserService.findByTenantIdAndUsername(tenantId, username));
    }

    /**
     * 按用户编号和租户编号查询用户。
     *
     * @param id 用户编号
     * @param tenantId 租户编号
     * @return 内部用户展示对象，不存在返回 data=null
     */
    @GetMapping("/user/{id}")
    public R<InternalUserResp> detail(@PathVariable Long id, @RequestParam Long tenantId) {
        log.info("接收内部用户资料查询请求，id={}，tenantId={}", id, tenantId);
        return R.ok(internalUserService.findByIdAndTenantId(id, tenantId));
    }
}
