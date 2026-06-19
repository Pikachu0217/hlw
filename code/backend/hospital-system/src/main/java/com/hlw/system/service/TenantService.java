package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateTenantReq;
import com.hlw.system.domain.req.UpdateTenantReq;
import com.hlw.system.domain.resp.TenantResp;
import com.hlw.system.entity.SysTenantEntity;
import com.hlw.system.entity.SysTenantPackageEntity;
import com.hlw.system.mapper.SysTenantMapper;
import com.hlw.system.mapper.SysTenantPackageMapper;
import com.hlw.system.service.converter.TenantConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 租户聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** 租户数据访问组件。 */
    private final SysTenantMapper sysTenantMapper;
    /** 租户套餐数据访问组件。 */
    private final SysTenantPackageMapper sysTenantPackageMapper;
    /** 租户展示对象转换器。 */
    private final TenantConverter tenantConverter;

    /**
     * 分页查询租户列表。
     *
     * @param query 分页查询条件
     * @return 租户分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<TenantResp> listTenants(PageQuery query) {
        log.info("查询租户列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysTenantEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysTenantEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(item -> item.like(SysTenantEntity::getCompanyName, query.getKeyword())
                .or()
                .like(SysTenantEntity::getTenantId, query.getKeyword())
                .or()
                .like(SysTenantEntity::getContactUserName, query.getKeyword()));
        }
        wrapper.orderByAsc(SysTenantEntity::getId);
        Page<SysTenantEntity> page = sysTenantMapper.selectPage(query.toPage(), wrapper);
        Map<Long, SysTenantPackageEntity> packageMap = loadPackageMap(page.getRecords());
        List<TenantResp> records = page.getRecords().stream()
            .map(tenant -> tenantConverter.toTenantVO(tenant, packageMap.get(tenant.getPackageId())))
            .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建租户。
     *
     * @param request 租户创建请求
     * @return 租户展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantResp createTenant(CreateTenantReq request) {
        log.info("创建租户，companyName={}，packageId={}", request.getCompanyName(), request.getPackageId());
        SysTenantEntity entity = new SysTenantEntity();
        entity.setTenantId(nextTenantId());
        fillTenant(entity, request);
        entity.setDeleted(DeletedStatusEnum.NOT_DELETED.getType());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysTenantMapper.insert(entity);
        return tenantConverter.toTenantVO(entity, loadPackage(entity.getPackageId()));
    }

    /**
     * 查询租户详情。
     *
     * @param id 租户表主键
     * @return 租户展示对象
     */
    @Transactional(readOnly = true)
    public TenantResp getTenant(Long id) {
        log.info("查询租户详情，id={}", id);
        SysTenantEntity entity = requireTenant(id);
        return tenantConverter.toTenantVO(entity, loadPackage(entity.getPackageId()));
    }

    /**
     * 更新租户。
     *
     * @param id 租户表主键
     * @param request 租户更新请求
     * @return 租户展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantResp updateTenant(Long id, UpdateTenantReq request) {
        log.info("更新租户，id={}，companyName={}", id, request.getCompanyName());
        SysTenantEntity entity = requireTenant(id);
        entity.setContactUserName(request.getContactUserName());
        entity.setContactPhone(request.getContactPhone());
        entity.setCompanyName(request.getCompanyName());
        entity.setLicenseNumber(request.getLicenseNumber());
        entity.setAddress(request.getAddress());
        entity.setIntro(request.getIntro());
        entity.setDomain(request.getDomain());
        entity.setRemark(request.getRemark());
        entity.setPackageId(request.getPackageId());
        entity.setExpireTime(parseTime(request.getExpireTime()));
        entity.setAccountCount(DefaultValueUtils.defaultIfNull(request.getAccountCount(), -1));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), "0"));
        entity.setUpdateTime(LocalDateTime.now());
        sysTenantMapper.updateById(entity);
        return tenantConverter.toTenantVO(entity, loadPackage(entity.getPackageId()));
    }

    /**
     * 删除租户。
     *
     * @param id 租户表主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenant(Long id) {
        log.info("删除租户，id={}", id);
        SysTenantEntity entity = requireTenant(id);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        entity.setUpdateTime(LocalDateTime.now());
        sysTenantMapper.updateById(entity);
    }

    /**
     * 填充租户实体字段。
     *
     * @param entity 租户实体
     * @param request 租户请求
     */
    private void fillTenant(SysTenantEntity entity, CreateTenantReq request) {
        entity.setContactUserName(request.getContactUserName());
        entity.setContactPhone(request.getContactPhone());
        entity.setCompanyName(request.getCompanyName());
        entity.setLicenseNumber(request.getLicenseNumber());
        entity.setAddress(request.getAddress());
        entity.setIntro(request.getIntro());
        entity.setDomain(request.getDomain());
        entity.setRemark(request.getRemark());
        entity.setPackageId(request.getPackageId());
        entity.setExpireTime(parseTime(request.getExpireTime()));
        entity.setAccountCount(DefaultValueUtils.defaultIfNull(request.getAccountCount(), -1));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), "0"));
    }

    /**
     * 生成租户编号。
     *
     * @return 租户编号
     */
    private String nextTenantId() {
        return System.currentTimeMillis() + String.valueOf(ThreadLocalRandom.current().nextInt(1000, 9999));
    }

    /**
     * 解析时间字符串。
     *
     * @param value 时间字符串
     * @return 时间对象
     */
    private LocalDateTime parseTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return LocalDateTime.parse(value, DATE_TIME_FORMATTER);
    }

    /**
     * 加载租户套餐映射。
     *
     * @param tenants 租户列表
     * @return 套餐映射
     */
    private Map<Long, SysTenantPackageEntity> loadPackageMap(List<SysTenantEntity> tenants) {
        List<Long> packageIds = tenants.stream().map(SysTenantEntity::getPackageId).filter(java.util.Objects::nonNull).distinct().toList();
        if (packageIds.isEmpty()) {
            return Map.of();
        }
        return sysTenantPackageMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysTenantPackageEntity::getDeleted)
                .in(SysTenantPackageEntity::getId, packageIds)).stream()
            .collect(Collectors.toMap(SysTenantPackageEntity::getId, item -> item, (left, right) -> left));
    }

    /**
     * 加载租户套餐。
     *
     * @param packageId 套餐编号
     * @return 租户套餐实体
     */
    private SysTenantPackageEntity loadPackage(Long packageId) {
        if (packageId == null) {
            return null;
        }
        return sysTenantPackageMapper.selectById(packageId);
    }

    /**
     * 校验租户存在。
     *
     * @param id 租户表主键
     * @return 租户实体
     */
    private SysTenantEntity requireTenant(Long id) {
        return MybatisTenantHelpers.requireEntity(sysTenantMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysTenantEntity::getDeleted)
                .eq(SysTenantEntity::getId, id)
                .last("limit 1")), "租户不存在");
    }
}
