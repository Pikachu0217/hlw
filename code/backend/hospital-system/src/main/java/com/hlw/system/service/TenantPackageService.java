package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.constants.SystemTenantConstants;
import com.hlw.system.domain.req.CreateTenantPackageReq;
import com.hlw.system.domain.resp.TenantPackageResp;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.entity.SysTenantPackageEntity;
import com.hlw.system.entity.SysTenantPackageMenuEntity;
import com.hlw.system.mapper.SysMenuMapper;
import com.hlw.system.mapper.SysTenantPackageMapper;
import com.hlw.system.mapper.SysTenantPackageMenuMapper;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.service.support.SystemDefaultDataGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 租户套餐聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantPackageService {
    /** 租户套餐数据访问组件。 */
    private final SysTenantPackageMapper sysTenantPackageMapper;
    /** 租户套餐菜单关系数据访问组件。 */
    private final SysTenantPackageMenuMapper sysTenantPackageMenuMapper;
    /** 菜单数据访问组件。 */
    private final SysMenuMapper sysMenuMapper;

    /**
     * 分页查询租户套餐。
     *
     * @param query 分页查询条件
     * @return 租户套餐分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<TenantPackageResp> listPackages(PageQuery query) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台租户可以查询租户套餐列表");
        log.info("查询租户套餐列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysTenantPackageEntity> wrapper = new LambdaQueryWrapper<SysTenantPackageEntity>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysTenantPackageEntity::getPackageName, query.getKeyword());
        }
        wrapper.orderByAsc(SysTenantPackageEntity::getId);
        Page<SysTenantPackageEntity> page = sysTenantPackageMapper.selectPage(query.toPage(), wrapper);
        List<TenantPackageResp> records = page.getRecords().stream().map(this::toResp).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建租户套餐。
     *
     * @param request 租户套餐创建请求
     * @return 租户套餐展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantPackageResp createPackage(CreateTenantPackageReq request) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台租户可以创建租户套餐");
        log.info("创建租户套餐，packageName={}，menuCount={}", request.getPackageName(), countMenuIds(request.getMenuIds()));
        SysTenantPackageEntity entity = new SysTenantPackageEntity();
        entity.setTenantId(SystemTenantConstants.PLATFORM_TENANT_ID);
        fillPackage(entity, request);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysTenantPackageMapper.insert(entity);
        replacePackageMenus(entity.getId(), request.getMenuIds());
        return toResp(entity);
    }

    /**
     * 查询租户套餐详情。
     *
     * @param id 套餐编号
     * @return 租户套餐展示对象
     */
    @Transactional(readOnly = true)
    public TenantPackageResp getPackage(Long id) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台租户可以查询租户套餐详情");
        log.info("查询租户套餐详情，id={}", id);
        return toResp(requirePackage(id));
    }

    /**
     * 更新租户套餐。
     *
     * @param id 套餐编号
     * @param request 租户套餐更新请求
     * @return 租户套餐展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantPackageResp updatePackage(Long id, CreateTenantPackageReq request) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台租户可以更新租户套餐");
        log.info("更新租户套餐，id={}，packageName={}，menuCount={}",
            id, request.getPackageName(), countMenuIds(request.getMenuIds()));
        SysTenantPackageEntity entity = requirePackage(id);
        SystemDefaultDataGuard.ensureCanUpdate(entity.getIsDefault(), "套餐");
        fillPackage(entity, request);
        entity.setUpdateTime(LocalDateTime.now());
        sysTenantPackageMapper.updateById(entity);
        replacePackageMenus(id, request.getMenuIds());
        return toResp(entity);
    }

    /**
     * 删除租户套餐。
     *
     * @param id 套餐编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePackage(Long id) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台租户可以删除租户套餐");
        log.info("删除租户套餐，id={}", id);
        SysTenantPackageEntity entity = requirePackage(id);
        SystemDefaultDataGuard.ensureCanDelete(entity.getIsDefault(), "套餐");
        sysTenantPackageMenuMapper.physicalDeleteByPackageId(SystemTenantConstants.PLATFORM_TENANT_ID, id);
        sysTenantPackageMapper.deleteById(id);
    }

    /**
     * 替换租户套餐菜单关系。
     *
     * @param packageId 套餐编号
     * @param menuIds 菜单编号列表
     */
    private void replacePackageMenus(Long packageId, List<Long> menuIds) {
        List<Long> distinctMenuIds = normalizeMenuIds(menuIds);
        log.info("替换租户套餐菜单绑定，packageId={}，menuIds={}", packageId, distinctMenuIds);
        distinctMenuIds.forEach(this::requireMenu);
        sysTenantPackageMenuMapper.physicalDeleteByPackageId(SystemTenantConstants.PLATFORM_TENANT_ID, packageId);
        if (distinctMenuIds.isEmpty()) {
            return;
        }
        for (Long menuId : distinctMenuIds) {
            SysTenantPackageMenuEntity relation = new SysTenantPackageMenuEntity();
            relation.setTenantId(SystemTenantConstants.PLATFORM_TENANT_ID);
            relation.setPackageId(packageId);
            relation.setMenuId(menuId);
            sysTenantPackageMenuMapper.insert(relation);
        }
    }

    /**
     * 转换租户套餐展示对象。
     *
     * @param entity 租户套餐实体
     * @return 租户套餐展示对象
     */
    private TenantPackageResp toResp(SysTenantPackageEntity entity) {
        TenantPackageResp resp = new TenantPackageResp();
        resp.setId(entity.getId());
        resp.setPackageName(entity.getPackageName());
        resp.setRemark(entity.getRemark());
        resp.setStatus(entity.getStatus());
        resp.setIsDefault(entity.getIsDefault());
        resp.setMenuIds(sysTenantPackageMenuMapper.selectList(new LambdaQueryWrapper<SysTenantPackageMenuEntity>()
                .eq(SysTenantPackageMenuEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID)
                .eq(SysTenantPackageMenuEntity::getPackageId, entity.getId())
                .orderByAsc(SysTenantPackageMenuEntity::getMenuId))
            .stream()
            .map(SysTenantPackageMenuEntity::getMenuId)
            .toList());
        return resp;
    }

    /**
     * 统计菜单编号数量。
     *
     * @param menuIds 菜单编号列表
     * @return 菜单编号数量
     */
    private int countMenuIds(List<Long> menuIds) {
        return menuIds == null ? 0 : menuIds.size();
    }

    /**
     * 标准化租户套餐菜单编号。
     *
     * @param menuIds 菜单编号列表
     * @return 去重后的菜单编号列表
     */
    private List<Long> normalizeMenuIds(List<Long> menuIds) {
        if (menuIds == null) {
            return List.of();
        }
        List<Long> distinctMenuIds = menuIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        return appendParentMenuIds(distinctMenuIds);
    }

    /**
     * 校验菜单存在。
     *
     * @param menuId 菜单编号
     */
    private void requireMenu(Long menuId) {
        Long count = sysMenuMapper.selectCount(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID)
            .eq(SysMenuEntity::getId, menuId));
        if (count == null || count == 0) {
            log.warn("租户套餐菜单绑定失败，菜单不存在，menuId={}", menuId);
            throw new BizException(404, "菜单不存在");
        }
    }

    /**
     * 为套餐菜单补齐平台模板父菜单编号，保证后续租户复制时父子层级完整。
     *
     * @param menuIds 菜单编号列表
     * @return 补齐父级后的菜单编号列表
     */
    private List<Long> appendParentMenuIds(List<Long> menuIds) {
        if (menuIds.isEmpty()) {
            return List.of();
        }
        List<SysMenuEntity> allPlatformMenus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID));
        Map<Long, SysMenuEntity> menuMap = allPlatformMenus.stream()
            .collect(Collectors.toMap(SysMenuEntity::getId, Function.identity(), (left, right) -> left));
        Set<Long> result = new LinkedHashSet<>(menuIds);
        for (Long menuId : menuIds) {
            appendParentMenuId(menuId, menuMap, result, new LinkedHashSet<>());
        }
        return List.copyOf(result);
    }

    /**
     * 递归补齐单个菜单的平台模板父菜单编号。
     *
     * @param menuId 菜单编号
     * @param menuMap 平台菜单映射
     * @param result 补齐结果集合
     * @param visited 已访问菜单编号集合
     */
    private void appendParentMenuId(Long menuId, Map<Long, SysMenuEntity> menuMap, Set<Long> result, Set<Long> visited) {
        if (!visited.add(menuId)) {
            log.warn("租户套餐菜单父级存在循环引用，menuId={}", menuId);
            return;
        }
        SysMenuEntity menu = menuMap.get(menuId);
        if (menu == null || menu.getParentId() == null || SystemTenantConstants.ROOT_MENU_PARENT_ID.equals(menu.getParentId())) {
            return;
        }
        if (result.add(menu.getParentId())) {
            appendParentMenuId(menu.getParentId(), menuMap, result, visited);
        }
    }

    /**
     * 填充租户套餐实体。
     *
     * @param entity 租户套餐实体
     * @param request 租户套餐请求
     */
    private void fillPackage(SysTenantPackageEntity entity, CreateTenantPackageReq request) {
        entity.setPackageName(request.getPackageName());
        entity.setRemark(request.getRemark());
        entity.setStatus(DefaultValueUtils.defaultIfNull(request.getStatus(), SystemTenantConstants.STATUS_NORMAL_VALUE));
    }

    /**
     * 校验租户套餐存在。
     *
     * @param id 套餐编号
     * @return 租户套餐实体
     */
    private SysTenantPackageEntity requirePackage(Long id) {
        return MybatisTenantHelpers.requireEntity(sysTenantPackageMapper.selectOne(
            new LambdaQueryWrapper<SysTenantPackageEntity>()
                .eq(SysTenantPackageEntity::getId, id)
                .last("limit 1")), "租户套餐不存在");
    }
}
