package com.hlw.system.controller;

import com.hlw.common.core.domain.R;
import com.hlw.system.domain.resp.RouterResp;
import com.hlw.system.domain.resp.UserInfoResp;
import com.hlw.system.service.SystemProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 当前登录用户资料与路由控制器。
 */
@RestController
@RequestMapping("/system")
@RequiredArgsConstructor
@Slf4j
public class SystemProfileController {
    /** 当前登录用户资料与路由聚合服务。 */
    private final SystemProfileService systemProfileService;

    /**
     * 查询当前登录用户信息。
     *
     * @return 当前登录用户信息
     */
    @GetMapping("/getInfo")
    public R<UserInfoResp> getInfo() {
        log.info("查询当前登录用户信息");
        return R.ok(systemProfileService.getInfo());
    }

    /**
     * 查询当前登录用户路由树。
     *
     * @return 前端路由树
     */
    @GetMapping("/getRouters")
    public R<List<RouterResp>> getRouters() {
        log.info("查询当前登录用户路由树");
        return R.ok(systemProfileService.getRouters());
    }
}
