package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateMenuReq;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.mapper.SysMenuMapper;
import com.hlw.system.service.converter.MenuConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.domain.resp.MenuResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 菜单聚合服务，负责菜单的查询与创建编排。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {
    private static final String DEFAULT_MENU_TYPE = "菜单";

    /** 菜单数据访问组件。 */
    private final SysMenuMapper sysMenuMapper;
    /** 菜单展示对象转换器。 */
    private final MenuConverter menuConverter;

    /**
     * 分页查询菜单列表，按父级、排序、主键升序排列。
     *
     * @param query 分页查询条件
     * @return 菜单分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<MenuResp> listMenus(PageQuery query) {
        log.info("查询系统菜单列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());

        Page<SysMenuEntity> page = query.toPage();
        LambdaQueryWrapper<SysMenuEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysMenuEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysMenuEntity::getMenuName, query.getKeyword());
        }
        wrapper.orderByAsc(SysMenuEntity::getParentId)
            .orderByAsc(SysMenuEntity::getSort)
            .orderByAsc(SysMenuEntity::getId);

        Page<SysMenuEntity> result = sysMenuMapper.selectPage(page, wrapper);
        List<MenuResp> records = result.getRecords().stream()
            .map(menuConverter::toMenuVO)
            .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 创建菜单。
     *
     * @param request 创建菜单请求
     * @return 新建菜单展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuResp createMenu(CreateMenuReq request) {
        log.info("创建菜单，menuName={}，permission={}，routePath={}",
            request.getMenuName(), request.getPermission(), request.getRoutePath());
        SysMenuEntity entity = new SysMenuEntity();
        entity.setMenuName(request.getMenuName());
        entity.setPermission(request.getPermission());
        entity.setRoutePath(request.getRoutePath());
        entity.setMenuType(DefaultValueUtils.defaultIfBlank(request.getMenuType(), DEFAULT_MENU_TYPE));
        entity.setParentId(DefaultValueUtils.defaultIfNull(request.getParentId(), 0L));
        entity.setSort(DefaultValueUtils.defaultIfNull(request.getSort(), 0));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setDeleted(0);
        sysMenuMapper.insert(entity);
        return menuConverter.toMenuVO(entity);
    }

    /**
     * 查询菜单详情。
     *
     * @param menuId 菜单编号
     * @return 菜单展示对象
     */
    @Transactional(readOnly = true)
    public MenuResp getMenu(Long menuId) {
        log.info("查询系统菜单详情，menuId={}", menuId);
        return menuConverter.toMenuVO(requireActiveMenu(menuId));
    }

    /**
     * 更新菜单。
     *
     * @param menuId 菜单编号
     * @param request 菜单更新请求
     * @return 更新后的菜单展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuResp updateMenu(Long menuId, CreateMenuReq request) {
        log.info("更新系统菜单，menuId={}，menuName={}，permission={}", menuId, request.getMenuName(), request.getPermission());
        SysMenuEntity entity = requireActiveMenu(menuId);
        entity.setMenuName(request.getMenuName());
        entity.setPermission(request.getPermission());
        entity.setRoutePath(request.getRoutePath());
        entity.setMenuType(DefaultValueUtils.defaultIfBlank(request.getMenuType(), DEFAULT_MENU_TYPE));
        entity.setParentId(DefaultValueUtils.defaultIfNull(request.getParentId(), 0L));
        entity.setSort(DefaultValueUtils.defaultIfNull(request.getSort(), 0));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        sysMenuMapper.updateById(entity);
        return menuConverter.toMenuVO(entity);
    }

    /**
     * 删除菜单。
     *
     * @param menuId 菜单编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long menuId) {
        log.info("删除系统菜单，menuId={}", menuId);
        SysMenuEntity entity = requireActiveMenu(menuId);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        sysMenuMapper.updateById(entity);
    }

    /**
     * 校验菜单处于可用状态。
     *
     * @param menuId 菜单编号
     * @return 菜单实体
     */
    private SysMenuEntity requireActiveMenu(Long menuId) {
        return MybatisTenantHelpers.requireEntity(sysMenuMapper.selectOne(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getDeleted, 0)
            .eq(SysMenuEntity::getId, menuId)
            .last("limit 1")), "菜单不存在");
    }
}
