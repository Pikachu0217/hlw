package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.constants.CommonConstants;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateTenantPackageReq;
import com.hlw.system.domain.resp.TenantPackageResp;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.entity.SysTenantPackageEntity;
import com.hlw.system.entity.SysTenantPackageMenuEntity;
import com.hlw.system.mapper.SysMenuMapper;
import com.hlw.system.mapper.SysTenantPackageMapper;
import com.hlw.system.mapper.SysTenantPackageMenuMapper;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

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
        entity.setTenantId(String.valueOf(CommonConstants.PLATFORM_TENANT_ID));
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
        requirePackage(id);
        sysTenantPackageMenuMapper.physicalDeleteByPackageId(String.valueOf(CommonConstants.PLATFORM_TENANT_ID), id);
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
        sysTenantPackageMenuMapper.physicalDeleteByPackageId(String.valueOf(CommonConstants.PLATFORM_TENANT_ID), packageId);
        if (distinctMenuIds.isEmpty()) {
            return;
        }
        for (Long menuId : distinctMenuIds) {
            SysTenantPackageMenuEntity relation = new SysTenantPackageMenuEntity();
            relation.setTenantId(String.valueOf(CommonConstants.PLATFORM_TENANT_ID));
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
        resp.setMenuIds(sysTenantPackageMenuMapper.selectList(new LambdaQueryWrapper<SysTenantPackageMenuEntity>()
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
        return menuIds.stream()
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    /**
     * 校验菜单存在。
     *
     * @param menuId 菜单编号
     */
    private void requireMenu(Long menuId) {
        Long count = sysMenuMapper.selectCount(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getId, menuId));
        if (count == null || count == 0) {
            log.warn("租户套餐菜单绑定失败，菜单不存在，menuId={}", menuId);
            throw new BizException(404, "菜单不存在");
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
        entity.setStatus(DefaultValueUtils.defaultIfNull(request.getStatus(), 0));
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
