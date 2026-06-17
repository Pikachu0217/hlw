package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.dto.CreateMenuRequest;
import com.hlw.system.service.MenuService;
import com.hlw.system.vo.MenuVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单管理控制器。
 */
@RestController
@RequestMapping("/system/menu")
@RequiredArgsConstructor
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
    public R<PageResult<MenuVO>> list(PageQuery query) {
        return R.ok(menuService.listMenus(query));
    }

    /**
     * 创建菜单。
     *
     * @param request 菜单创建命令
     * @return 创建后的菜单
     */
    @PostMapping
    public R<MenuVO> createMenu(@Valid @RequestBody CreateMenuRequest request) {
        return R.ok(menuService.createMenu(request));
    }
}
