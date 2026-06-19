package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.constants.CommonConstants;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateMenuReq;
import com.hlw.system.domain.resp.MenuResp;
import com.hlw.system.domain.resp.RouterResp;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.mapper.SysMenuMapper;
import com.hlw.system.service.converter.MenuConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {
    /** 菜单数据访问组件。 */
    private final SysMenuMapper sysMenuMapper;
    /** 菜单展示对象转换器。 */
    private final MenuConverter menuConverter;

    /**
     * 分页查询菜单列表。
     *
     * @param query 分页查询条件
     * @return 菜单分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<MenuResp> listMenus(PageQuery query) {
        log.info("查询菜单列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysMenuEntity> wrapper = buildListWrapper(query);
        Page<SysMenuEntity> page = sysMenuMapper.selectPage(query.toPage(), wrapper);
        List<MenuResp> records = page.getRecords().stream().map(menuConverter::toMenuVO).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 查询可用菜单列表。
     *
     * @return 可用菜单列表
     */
    @Transactional(readOnly = true)
    public List<SysMenuEntity> listEnabledMenus() {
        log.info("查询可用菜单列表");
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getStatus, "0")
            .orderByAsc(SysMenuEntity::getParentId)
            .orderByAsc(SysMenuEntity::getOrderNum)
            .orderByAsc(SysMenuEntity::getId));
    }

    /**
     * 构建当前用户路由树。
     *
     * @param menus 菜单实体列表
     * @return 前端路由树
     */
    public List<RouterResp> buildRouters(List<SysMenuEntity> menus) {
        log.info("构建前端路由树，menuCount={}", menus.size());
        Map<Long, List<SysMenuEntity>> childrenMap = menus.stream()
            .filter(menu -> !"F".equals(menu.getMenuType()))
            .collect(Collectors.groupingBy(SysMenuEntity::getParentId));
        return childrenMap.getOrDefault(0L, List.of()).stream()
            .sorted(Comparator.comparing(SysMenuEntity::getOrderNum).thenComparing(SysMenuEntity::getId))
            .map(menu -> toRouter(menu, childrenMap))
            .toList();
    }

    /**
     * 创建菜单。
     *
     * @param request 菜单创建请求
     * @return 菜单展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuResp createMenu(CreateMenuReq request) {
        log.info("创建菜单，menuName={}，perms={}", request.getMenuName(), request.getPerms());
        SysMenuEntity entity = new SysMenuEntity();
        entity.setTenantId(String.valueOf(CommonConstants.PLATFORM_TENANT_ID));
        fillMenu(entity, request);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysMenuMapper.insert(entity);
        return menuConverter.toMenuVO(entity);
    }

    /**
     * 查询菜单详情。
     *
     * @param id 菜单编号
     * @return 菜单展示对象
     */
    @Transactional(readOnly = true)
    public MenuResp getMenu(Long id) {
        log.info("查询菜单详情，id={}", id);
        return menuConverter.toMenuVO(requireMenu(id));
    }

    /**
     * 更新菜单。
     *
     * @param id 菜单编号
     * @param request 菜单更新请求
     * @return 菜单展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public MenuResp updateMenu(Long id, CreateMenuReq request) {
        log.info("更新菜单，id={}，menuName={}，perms={}", id, request.getMenuName(), request.getPerms());
        SysMenuEntity entity = requireMenu(id);
        fillMenu(entity, request);
        entity.setUpdateTime(LocalDateTime.now());
        sysMenuMapper.updateById(entity);
        return menuConverter.toMenuVO(entity);
    }

    /**
     * 删除菜单。
     *
     * @param id 菜单编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteMenu(Long id) {
        log.info("删除菜单，id={}", id);
        requireMenu(id);
        sysMenuMapper.deleteById(id);
    }

    /**
     * 按编号查询菜单实体。
     *
     * @param id 菜单编号
     * @return 菜单实体
     */
    @Transactional(readOnly = true)
    public SysMenuEntity requireMenu(Long id) {
        return MybatisTenantHelpers.requireEntity(sysMenuMapper.selectOne(
            new LambdaQueryWrapper<SysMenuEntity>()
                .eq(SysMenuEntity::getId, id)
                .last("limit 1")), "菜单不存在");
    }

    /**
     * 构造菜单列表查询条件。
     *
     * @param query 查询条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<SysMenuEntity> buildListWrapper(PageQuery query) {
        LambdaQueryWrapper<SysMenuEntity> wrapper = new LambdaQueryWrapper<SysMenuEntity>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(item -> item.like(SysMenuEntity::getMenuName, query.getKeyword())
                .or()
                .like(SysMenuEntity::getPerms, query.getKeyword()));
        }
        return wrapper.orderByAsc(SysMenuEntity::getParentId).orderByAsc(SysMenuEntity::getOrderNum).orderByAsc(SysMenuEntity::getId);
    }

    /**
     * 填充菜单实体字段。
     *
     * @param entity 菜单实体
     * @param request 菜单请求
     */
    private void fillMenu(SysMenuEntity entity, CreateMenuReq request) {
        entity.setMenuName(request.getMenuName());
        entity.setParentId(DefaultValueUtils.defaultIfNull(request.getParentId(), 0L));
        entity.setOrderNum(DefaultValueUtils.defaultIfNull(request.getOrderNum(), 0));
        entity.setPath(DefaultValueUtils.defaultIfBlank(request.getPath(), ""));
        entity.setComponent(request.getComponent());
        entity.setIsFrame(DefaultValueUtils.defaultIfNull(request.getIsFrame(), 1));
        entity.setMenuType(DefaultValueUtils.defaultIfBlank(request.getMenuType(), "C"));
        entity.setVisible(DefaultValueUtils.defaultIfBlank(request.getVisible(), "0"));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), "0"));
        entity.setPerms(request.getPerms());
        entity.setIcon(DefaultValueUtils.defaultIfBlank(request.getIcon(), "#"));
        entity.setRemark(request.getRemark());
    }

    /**
     * 转换路由节点。
     *
     * @param menu 菜单实体
     * @param childrenMap 子菜单映射
     * @return 路由节点
     */
    private RouterResp toRouter(SysMenuEntity menu, Map<Long, List<SysMenuEntity>> childrenMap) {
        RouterResp router = new RouterResp();
        router.setName(menu.getMenuName());
        router.setPath(menu.getPath());
        router.setComponent(menu.getComponent());
        router.setHidden("1".equals(menu.getVisible()));
        RouterResp.Meta meta = new RouterResp.Meta();
        meta.setTitle(menu.getMenuName());
        meta.setIcon(menu.getIcon());
        router.setMeta(meta);
        List<RouterResp> children = new ArrayList<>();
        for (SysMenuEntity child : childrenMap.getOrDefault(menu.getId(), List.of())) {
            children.add(toRouter(child, childrenMap));
        }
        router.setChildren(children);
        return router;
    }
}
