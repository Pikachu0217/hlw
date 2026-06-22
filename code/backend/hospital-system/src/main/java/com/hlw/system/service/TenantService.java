package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.HttpStatusEnum;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.constants.SystemTenantConstants;
import com.hlw.system.domain.req.CreateTenantReq;
import com.hlw.system.domain.req.UpdateTenantReq;
import com.hlw.system.domain.resp.TenantOptionResp;
import com.hlw.system.domain.resp.TenantResp;
import com.hlw.system.entity.SysTenantEntity;
import com.hlw.system.entity.SysTenantPackageEntity;
import com.hlw.system.mapper.SysTenantMapper;
import com.hlw.system.mapper.SysTenantPackageMapper;
import com.hlw.system.service.converter.TenantConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.service.support.SystemDefaultDataGuard;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    /** 租户初始化服务。 */
    private final TenantBootstrapService tenantBootstrapService;

    /**
     * 分页查询租户列表。
     *
     * @param query 分页查询条件
     * @return 租户分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<TenantResp> listTenants(PageQuery query) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台租户可以查询租户列表");
        log.info("查询租户列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysTenantEntity> wrapper = new LambdaQueryWrapper<SysTenantEntity>();
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
     * 查询登录前可选择的租户选项。
     *
     * @return 租户选项列表
     */
    @Transactional(readOnly = true)
    public List<TenantOptionResp> listTenantOptions() {
        log.info("查询登录前租户选项");
        return sysTenantMapper.selectList(new LambdaQueryWrapper<SysTenantEntity>()
                .eq(SysTenantEntity::getStatus, "0")
                .orderByAsc(SysTenantEntity::getId))
            .stream()
            .map(this::toTenantOption)
            .toList();
    }

    /**
     * 创建租户。
     *
     * @param request 租户创建请求
     * @return 租户展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantResp createTenant(CreateTenantReq request) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台租户可以创建租户");
        log.info("创建租户，companyName={}，packageId={}", request.getCompanyName(), request.getPackageId());
        SysTenantEntity entity = new SysTenantEntity();
        entity.setTenantId(nextTenantId());
        fillTenant(entity, request);
        SysTenantPackageEntity packageEntity = requirePackage(entity.getPackageId());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysTenantMapper.insert(entity);
        tenantBootstrapService.initializeTenant(entity);
        return tenantConverter.toTenantVO(entity, packageEntity);
    }

    /**
     * 查询租户详情。
     *
     * @param id 租户表主键
     * @return 租户展示对象
     */
    @Transactional(readOnly = true)
    public TenantResp getTenant(Long id) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台租户可以查询租户详情");
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
        MybatisTenantHelpers.ensurePlatformContext("只有平台租户可以更新租户");
        log.info("更新租户，id={}，companyName={}，packageId={}", id, request.getCompanyName(), request.getPackageId());
        SysTenantEntity entity = requireTenant(id);
        SystemDefaultDataGuard.ensureCanUpdate(entity.getIsDefault(), "租户");
        Long oldPackageId = entity.getPackageId();
        SysTenantPackageEntity packageEntity = requirePackage(request.getPackageId());
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
        if (!Objects.equals(oldPackageId, entity.getPackageId())) {
            if (SystemTenantConstants.PLATFORM_TENANT_ID.equals(entity.getTenantId())) {
                log.info("平台租户套餐发生变更，跳过菜单权限重建，tenantId={}，oldPackageId={}，newPackageId={}",
                    entity.getTenantId(), oldPackageId, entity.getPackageId());
            } else {
                log.info("租户套餐发生变更，开始重建套餐权限绑定，tenantId={}，oldPackageId={}，newPackageId={}",
                    entity.getTenantId(), oldPackageId, entity.getPackageId());
                tenantBootstrapService.rebuildTenantPackageBindings(entity);
            }
        }
        return tenantConverter.toTenantVO(entity, packageEntity);
    }

    /**
     * 删除租户。
     *
     * @param id 租户表主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenant(Long id) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台租户可以删除租户");
        log.info("删除租户，id={}", id);
        SysTenantEntity entity = requireTenant(id);
        SystemDefaultDataGuard.ensureCanDelete(entity.getIsDefault(), "租户");
        sysTenantMapper.deleteById(id);
    }

    /**
     * 转换为登录前租户选项展示对象。
     *
     * @param entity 租户实体
     * @return 租户选项展示对象
     */
    private TenantOptionResp toTenantOption(SysTenantEntity entity) {
        TenantOptionResp resp = new TenantOptionResp();
        resp.setId(entity.getId());
        resp.setTenantId(entity.getTenantId());
        resp.setCompanyName(entity.getCompanyName());
        resp.setStatus(entity.getStatus());
        return resp;
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
        return sysTenantPackageMapper.selectList(new LambdaQueryWrapper<SysTenantPackageEntity>()
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
     * 校验租户套餐存在。
     *
     * @param packageId 租户套餐编号
     * @return 租户套餐实体
     */
    private SysTenantPackageEntity requirePackage(Long packageId) {
        if (packageId == null) {
            throw new BizException(HttpStatusEnum.TENANT_PACKAGE_REQUIRED);
        }
        return MybatisTenantHelpers.requireEntity(sysTenantPackageMapper.selectOne(
            new LambdaQueryWrapper<SysTenantPackageEntity>()
                .eq(SysTenantPackageEntity::getId, packageId)
                .last("limit 1")), "租户套餐不存在");
    }

    /**
     * 校验租户存在。
     *
     * @param id 租户表主键
     * @return 租户实体
     */
    private SysTenantEntity requireTenant(Long id) {
        return MybatisTenantHelpers.requireEntity(sysTenantMapper.selectOne(
            new LambdaQueryWrapper<SysTenantEntity>()
                .eq(SysTenantEntity::getId, id)
                .last("limit 1")), "租户不存在");
    }
}
