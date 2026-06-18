package com.hlw.gateway.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.gateway.domain.req.CreateRouteConfigReq;
import com.hlw.gateway.domain.req.UpdateRouteConfigReq;
import com.hlw.gateway.domain.resp.RouteConfigResp;
import com.hlw.gateway.service.RouteConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 网关路由配置管理控制器。
 */
@RestController
@RequestMapping("/gateway/route")
@RequiredArgsConstructor
@Slf4j
public class RouteConfigController {
    /** 网关路由配置聚合服务。 */
    private final RouteConfigService routeConfigService;

    /**
     * 分页查询网关路由配置列表。
     *
     * @param query 分页查询参数
     * @return 网关路由配置分页结果
     */
    @GetMapping
    public R<PageResult<RouteConfigResp>> list(PageQuery query) {
        log.info("查询网关路由配置列表，keyword={}", query.getKeyword());
        return R.ok(routeConfigService.listRoutes(query));
    }

    /**
     * 创建网关路由配置。
     *
     * @param request 路由配置创建命令
     * @return 创建后的路由配置
     */
    @PostMapping
    public R<RouteConfigResp> createRoute(@Valid @RequestBody CreateRouteConfigReq request) {
        log.info("创建网关路由配置，routeCode={}", request.getRouteCode());
        return R.ok(routeConfigService.createRoute(request));
    }

    /**
     * 查询网关路由配置详情。
     *
     * @param id 路由配置编号
     * @return 路由配置详情
     */
    @GetMapping("/{id}")
    public R<RouteConfigResp> detail(@PathVariable Long id) {
        log.info("查询网关路由配置详情，id={}", id);
        return R.ok(routeConfigService.getRoute(id));
    }

    /**
     * 更新网关路由配置。
     *
     * @param id 路由配置编号
     * @param request 路由配置更新命令
     * @return 更新后的路由配置
     */
    @PutMapping("/{id}")
    public R<RouteConfigResp> updateRoute(@PathVariable Long id, @Valid @RequestBody UpdateRouteConfigReq request) {
        log.info("更新网关路由配置，id={}，routeCode={}", id, request.getRouteCode());
        return R.ok(routeConfigService.updateRoute(id, request));
    }

    /**
     * 删除网关路由配置。
     *
     * @param id 路由配置编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteRoute(@PathVariable Long id) {
        log.info("删除网关路由配置，id={}", id);
        routeConfigService.deleteRoute(id);
        return R.ok(null);
    }
}
