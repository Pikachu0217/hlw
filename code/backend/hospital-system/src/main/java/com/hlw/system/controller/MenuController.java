package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.domain.req.CreateMenuReq;
import com.hlw.system.service.MenuService;
import com.hlw.system.domain.resp.MenuResp;
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
 * 菜单管理控制器。
 */
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
@Slf4j
public class MenuController {
    /** 菜单聚合服务。 */
    private final MenuService menuService;

    /**
     * 分页查询菜单列表。
     *
     * @param query 分页查询参数
     * @return 菜单分页结果
     */
    @GetMapping
    public R<PageResult<MenuResp>> list(PageQuery query) {
        log.info("查询菜单列表，keyword={}", query.getKeyword());
        return R.ok(menuService.listMenus(query));
    }

    /**
     * 创建菜单。
     *
     * @param request 菜单创建命令
     * @return 创建后的菜单
     */
    @PostMapping
    public R<MenuResp> createMenu(@Valid @RequestBody CreateMenuReq request) {
        log.info("创建菜单，menuName={}，perms={}", request.getMenuName(), request.getPerms());
        return R.ok(menuService.createMenu(request));
    }

    /**
     * 查询菜单详情。
     *
     * @param id 菜单编号
     * @return 菜单详情
     */
    @GetMapping("/{id}")
    public R<MenuResp> detail(@PathVariable Long id) {
        log.info("查询菜单详情，id={}", id);
        return R.ok(menuService.getMenu(id));
    }

    /**
     * 更新菜单。
     *
     * @param id 菜单编号
     * @param request 菜单更新命令
     * @return 更新后的菜单
     */
    @PutMapping("/{id}")
    public R<MenuResp> updateMenu(@PathVariable Long id, @Valid @RequestBody CreateMenuReq request) {
        log.info("更新菜单，id={}，menuName={}，perms={}", id, request.getMenuName(), request.getPerms());
        return R.ok(menuService.updateMenu(id, request));
    }

    /**
     * 删除菜单。
     *
     * @param id 菜单编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteMenu(@PathVariable Long id) {
        log.info("删除菜单，id={}", id);
        menuService.deleteMenu(id);
        return R.ok(null);
    }
}
