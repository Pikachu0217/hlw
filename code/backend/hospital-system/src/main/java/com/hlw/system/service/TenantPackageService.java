package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateTenantPackageReq;
import com.hlw.system.domain.resp.TenantPackageResp;
import com.hlw.system.entity.SysTenantPackageEntity;
import com.hlw.system.entity.SysTenantPackageMenuEntity;
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

    /**
     * 分页查询租户套餐。
     *
     * @param query 分页查询条件
     * @return 租户套餐分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<TenantPackageResp> listPackages(PageQuery query) {
        log.info("查询租户套餐列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysTenantPackageEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysTenantPackageEntity::getDeleted);
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
        log.info("创建租户套餐，packageName={}", request.getPackageName());
        SysTenantPackageEntity entity = new SysTenantPackageEntity();
        fillPackage(entity, request);
        entity.setDeleted(DeletedStatusEnum.NOT_DELETED.getType());
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
        log.info("更新租户套餐，id={}，packageName={}", id, request.getPackageName());
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
        log.info("删除租户套餐，id={}", id);
        SysTenantPackageEntity entity = requirePackage(id);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        entity.setUpdateTime(LocalDateTime.now());
        sysTenantPackageMapper.updateById(entity);
    }

    /**
     * 替换租户套餐菜单关系。
     *
     * @param packageId 套餐编号
     * @param menuIds 菜单编号列表
     */
    private void replacePackageMenus(Long packageId, List<Long> menuIds) {
        sysTenantPackageMenuMapper.delete(new LambdaQueryWrapper<SysTenantPackageMenuEntity>()
            .eq(SysTenantPackageMenuEntity::getPackageId, packageId));
        if (menuIds == null || menuIds.isEmpty()) {
            return;
        }
        for (Long menuId : menuIds) {
            SysTenantPackageMenuEntity relation = new SysTenantPackageMenuEntity();
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
        resp.setKey(String.valueOf(entity.getId()));
        resp.setId(entity.getId());
        resp.setPackageName(entity.getPackageName());
        resp.setRemark(entity.getRemark());
        resp.setStatus(entity.getStatus());
        resp.setMenuIds(sysTenantPackageMenuMapper.selectList(new LambdaQueryWrapper<SysTenantPackageMenuEntity>()
                .eq(SysTenantPackageMenuEntity::getPackageId, entity.getId()))
            .stream()
            .map(SysTenantPackageMenuEntity::getMenuId)
            .toList());
        return resp;
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
            MybatisTenantHelpers.notDeletedWrapper(SysTenantPackageEntity::getDeleted)
                .eq(SysTenantPackageEntity::getId, id)
                .last("limit 1")), "租户套餐不存在");
    }
}
