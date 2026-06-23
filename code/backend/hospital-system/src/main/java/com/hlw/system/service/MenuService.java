package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.constants.SystemTenantConstants;
import com.hlw.system.domain.req.CreateMenuReq;
import com.hlw.system.domain.resp.MenuResp;
import com.hlw.system.domain.resp.RouterResp;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.mapper.SysMenuMapper;
import com.hlw.system.service.converter.MenuConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.service.support.SystemDefaultDataGuard;
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
    /** 顶级菜单父编号。 */
    private static final Long ROOT_MENU_PARENT_ID = 0L;
    /** 默认菜单排序值。 */
    private static final Integer DEFAULT_MENU_ORDER = 0;

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
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        log.info("查询菜单列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysMenuEntity> wrapper = buildListWrapper(query);
        wrapper.eq(SysMenuEntity::getTenantId, tenantId);
        List<SysMenuEntity> menus = sysMenuMapper.selectList(wrapper);
        List<MenuResp> records = buildMenuTree(menus);
        log.info("菜单树构建完成，rootCount={}，totalCount={}", records.size(), menus.size());
        return new PageResult<>(records, menus.size(), query.getPageNum(), query.getPageSize());
    }

    /**
     * 查询可用菜单列表。
     *
     * @return 可用菜单列表
     */
    @Transactional(readOnly = true)
    public List<SysMenuEntity> listEnabledMenus() {
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        log.info("查询可用菜单列表，tenantId={}", tenantId);
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getTenantId, tenantId)
            .eq(SysMenuEntity::getStatus, SystemTenantConstants.STATUS_NORMAL)
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
            .filter(menu -> !SystemTenantConstants.MENU_TYPE_BUTTON.equals(menu.getMenuType()))
            .collect(Collectors.groupingBy(SysMenuEntity::getParentId));
        return childrenMap.getOrDefault(ROOT_MENU_PARENT_ID, List.of()).stream()
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
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        log.info("创建菜单，tenantId={}，menuName={}，perms={}", tenantId, request.getMenuName(), request.getPerms());
        SysMenuEntity entity = new SysMenuEntity();
        entity.setTenantId(tenantId);
        entity.setIsDefault(SystemTenantConstants.NORMAL_DATA_FLAG);
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
        log.info("查询菜单详情，tenantId={}，id={}", MybatisTenantHelpers.currentTenantIdString(), id);
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
        log.info("更新菜单，tenantId={}，id={}，menuName={}，perms={}",
            MybatisTenantHelpers.currentTenantIdString(), id, request.getMenuName(), request.getPerms());
        SysMenuEntity entity = requireMenu(id);
        SystemDefaultDataGuard.ensureCanUpdate(entity.getIsDefault(), "菜单");
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
        log.info("删除菜单，tenantId={}，id={}", MybatisTenantHelpers.currentTenantIdString(), id);
        SysMenuEntity entity = requireMenu(id);
        SystemDefaultDataGuard.ensureCanDelete(entity.getIsDefault(), "菜单");
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
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        return MybatisTenantHelpers.requireEntity(sysMenuMapper.selectOne(
            new LambdaQueryWrapper<SysMenuEntity>()
                .eq(SysMenuEntity::getTenantId, tenantId)
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
     * 将菜单实体列表组装为菜单树。
     *
     * @param menus 菜单实体列表
     * @return 菜单树
     */
    private List<MenuResp> buildMenuTree(List<SysMenuEntity> menus) {
        Map<Long, SysMenuEntity> menuMap = menus.stream()
            .collect(Collectors.toMap(SysMenuEntity::getId, menu -> menu, (current, next) -> current));
        Map<Long, List<SysMenuEntity>> childrenMap = menus.stream()
            .collect(Collectors.groupingBy(menu -> DefaultValueUtils.defaultIfNull(menu.getParentId(), ROOT_MENU_PARENT_ID)));
        return menus.stream()
            .filter(menu -> isRootMenu(menu, menuMap))
            .sorted(menuComparator())
            .map(menu -> toMenuTreeNode(menu, childrenMap))
            .toList();
    }

    /**
     * 判断菜单是否为树根节点。
     *
     * @param menu 菜单实体
     * @param menuMap 菜单编号映射
     * @return 是否根节点
     */
    private boolean isRootMenu(SysMenuEntity menu, Map<Long, SysMenuEntity> menuMap) {
        Long parentId = DefaultValueUtils.defaultIfNull(menu.getParentId(), ROOT_MENU_PARENT_ID);
        return ROOT_MENU_PARENT_ID.equals(parentId) || !menuMap.containsKey(parentId);
    }

    /**
     * 转换菜单树节点。
     *
     * @param menu 菜单实体
     * @param childrenMap 子菜单映射
     * @return 菜单树节点
     */
    private MenuResp toMenuTreeNode(SysMenuEntity menu, Map<Long, List<SysMenuEntity>> childrenMap) {
        MenuResp node = menuConverter.toMenuVO(menu);
        List<MenuResp> children = childrenMap.getOrDefault(menu.getId(), List.of()).stream()
            .filter(child -> !child.getId().equals(menu.getId()))
            .sorted(menuComparator())
            .map(child -> toMenuTreeNode(child, childrenMap))
            .toList();
        node.setChildren(children);
        return node;
    }

    /**
     * 构造菜单排序器。
     *
     * @return 菜单排序器
     */
    private Comparator<SysMenuEntity> menuComparator() {
        return Comparator.comparing(
                (SysMenuEntity menu) -> DefaultValueUtils.defaultIfNull(menu.getOrderNum(), DEFAULT_MENU_ORDER))
            .thenComparing(SysMenuEntity::getId);
    }

    /**
     * 填充菜单实体字段。
     *
     * @param entity 菜单实体
     * @param request 菜单请求
     */
    private void fillMenu(SysMenuEntity entity, CreateMenuReq request) {
        entity.setMenuName(request.getMenuName());
        entity.setParentId(DefaultValueUtils.defaultIfNull(request.getParentId(), ROOT_MENU_PARENT_ID));
        entity.setOrderNum(DefaultValueUtils.defaultIfNull(request.getOrderNum(), DEFAULT_MENU_ORDER));
        entity.setPath(DefaultValueUtils.defaultIfBlank(request.getPath(), ""));
        entity.setComponent(request.getComponent());
        entity.setIsFrame(DefaultValueUtils.defaultIfNull(request.getIsFrame(), SystemTenantConstants.DEFAULT_MENU_FRAME));
        entity.setMenuType(DefaultValueUtils.defaultIfBlank(request.getMenuType(), SystemTenantConstants.DEFAULT_MENU_TYPE));
        entity.setVisible(DefaultValueUtils.defaultIfBlank(request.getVisible(), SystemTenantConstants.STATUS_NORMAL));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), SystemTenantConstants.STATUS_NORMAL));
        entity.setPerms(request.getPerms());
        entity.setIcon(DefaultValueUtils.defaultIfBlank(request.getIcon(), SystemTenantConstants.DEFAULT_MENU_ICON));
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
