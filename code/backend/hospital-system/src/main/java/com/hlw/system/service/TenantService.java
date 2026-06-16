package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TenantContext;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.dto.CreateTenantRequest;
import com.hlw.system.dto.UpdateTenantRequest;
import com.hlw.system.entity.SysTenantEntity;
import com.hlw.system.mapper.SysTenantMapper;
import com.hlw.system.service.converter.TenantConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.vo.TenantVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 租户聚合服务，负责租户的查询、创建、更新与删除编排。
 */
@Service
@RequiredArgsConstructor
public class TenantService {
    private static final Logger log = LoggerFactory.getLogger(TenantService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /** 租户数据访问组件。 */
    private final SysTenantMapper sysTenantMapper;
    /** 租户展示对象转换器。 */
    private final TenantConverter tenantConverter;

    /**
     * 分页查询租户列表。
     *
     * @param query 分页查询条件
     * @return 租户分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<TenantVO> listTenants(PageQuery query) {
        log.info("查询租户列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        Long currentTenantId = TenantContext.getTenantId();

        if (TenantContext.isPlatformRequest()) {
            log.info("平台上下文查询全部未删除租户");
            return pageAllUndeletedTenants(query);
        }
        if (isPublicTenantQuery(currentTenantId)) {
            log.info("公开租户列表查询全部未删除租户");
            return pageAllUndeletedTenants(query);
        }
        log.info("租户上下文查询当前租户，tenantId={}", currentTenantId);
        return pageCurrentTenant(query, currentTenantId);
    }

    /**
     * 创建租户。
     *
     * @param request 创建租户请求
     * @return 新建租户展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantVO createTenant(CreateTenantRequest request) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台上下文允许创建租户");
        log.info("创建租户，tenantName={}，packageName={}，adminName={}",
            request.getTenantName(), request.getPackageName(), request.getAdminName());
        SysTenantEntity entity = new SysTenantEntity();
        entity.setTenantId(nextTenantId());
        entity.setName(request.getTenantName());
        entity.setTenantName(request.getTenantName());
        entity.setPackageName(request.getPackageName());
        entity.setAdminName(request.getAdminName());
        entity.setExpireAt(parseDate(request.getExpireAt()));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setDeleted(0);
        assertTenantIdNotExists(entity.getTenantId());
        InterceptorIgnoreHelper.execute(MybatisTenantHelpers.ignoreTenantLine(), () -> sysTenantMapper.insert(entity));
        return tenantConverter.toTenantVO(entity);
    }

    /**
     * 查询租户详情。
     *
     * @param id 租户主键编号
     * @return 租户展示对象
     */
    @Transactional(readOnly = true)
    public TenantVO getTenant(Long id) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台上下文允许查询租户详情");
        log.info("查询租户详情，id={}", id);
        return tenantConverter.toTenantVO(requireActiveTenant(id));
    }

    /**
     * 更新租户信息。
     *
     * @param id 租户主键编号
     * @param request 更新租户请求
     * @return 更新后的租户展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public TenantVO updateTenant(Long id, UpdateTenantRequest request) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台上下文允许更新租户");
        log.info("更新租户，id={}，tenantName={}", id, request.getTenantName());
        SysTenantEntity entity = requireActiveTenant(id);
        entity.setTenantName(request.getTenantName());
        entity.setName(request.getTenantName());
        entity.setPackageName(request.getPackageName());
        entity.setAdminName(request.getAdminName());
        entity.setExpireAt(parseDate(request.getExpireAt()));
        entity.setStatus(request.getStatus());
        InterceptorIgnoreHelper.execute(MybatisTenantHelpers.ignoreTenantLine(), () -> sysTenantMapper.updateById(entity));
        return tenantConverter.toTenantVO(entity);
    }

    /**
     * 删除租户。
     *
     * @param id 租户主键编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTenant(Long id) {
        MybatisTenantHelpers.ensurePlatformContext("只有平台上下文允许删除租户");
        log.info("删除租户，id={}", id);
        SysTenantEntity entity = requireActiveTenant(id);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        InterceptorIgnoreHelper.execute(MybatisTenantHelpers.ignoreTenantLine(), () -> sysTenantMapper.updateById(entity));
    }

    /**
     * 判断当前请求是否为登录前公开租户查询。
     *
     * @param currentTenantId 当前租户编号
     * @return 是否公开查询
     */
    private boolean isPublicTenantQuery(Long currentTenantId) {
        return currentTenantId == null || currentTenantId <= 0L;
    }

    /**
     * 分页查询全部未删除租户，供平台上下文和登录前公开选择使用。
     *
     * @param query 分页查询条件
     * @return 租户分页结果
     */
    private PageResult<TenantVO> pageAllUndeletedTenants(PageQuery query) {
        Page<SysTenantEntity> page = query.toPage();
        LambdaQueryWrapper<SysTenantEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysTenantEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword();
            wrapper.and(w -> w.like(SysTenantEntity::getTenantName, keyword).or().like(SysTenantEntity::getName, keyword));
        }
        wrapper.orderByAsc(SysTenantEntity::getId);
        Page<SysTenantEntity> result = InterceptorIgnoreHelper.execute(
            MybatisTenantHelpers.ignoreTenantLine(),
            () -> sysTenantMapper.selectPage(page, wrapper)
        );
        return toPageResult(result);
    }

    /**
     * 分页查询当前租户上下文对应的租户。
     *
     * @param query 分页查询条件
     * @param currentTenantId 当前租户编号
     * @return 租户分页结果
     */
    private PageResult<TenantVO> pageCurrentTenant(PageQuery query, Long currentTenantId) {
        Page<SysTenantEntity> page = query.toPage();
        LambdaQueryWrapper<SysTenantEntity> wrapper = new LambdaQueryWrapper<SysTenantEntity>()
            .eq(SysTenantEntity::getDeleted, 0)
            .eq(SysTenantEntity::getTenantId, currentTenantId);
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword();
            wrapper.and(w -> w.like(SysTenantEntity::getTenantName, keyword).or().like(SysTenantEntity::getName, keyword));
        }
        wrapper.orderByAsc(SysTenantEntity::getId);
        Page<SysTenantEntity> result = sysTenantMapper.selectPage(page, wrapper);
        return toPageResult(result);
    }

    /**
     * 将分页结果转换为对外展示分页结果。
     *
     * @param result 数据库分页结果
     * @return 租户展示分页结果
     */
    private PageResult<TenantVO> toPageResult(Page<SysTenantEntity> result) {
        List<TenantVO> records = result.getRecords().stream()
            .map(tenantConverter::toTenantVO)
            .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 生成下一个租户编号。
     *
     * @return 租户编号
     */
    private Long nextTenantId() {
        return InterceptorIgnoreHelper.execute(MybatisTenantHelpers.ignoreTenantLine(), () ->
            sysTenantMapper.selectObjs(new QueryWrapper<SysTenantEntity>()
                .select("COALESCE(MAX(tenant_id), 0)"))
                .stream().findFirst()
                .map(o -> ((Number) o).longValue())
                .orElse(0L) + 1L
        );
    }

    /**
     * 校验租户编号未被占用。
     *
     * @param tenantId 租户编号
     */
    private void assertTenantIdNotExists(Long tenantId) {
        Long count = InterceptorIgnoreHelper.execute(
            MybatisTenantHelpers.ignoreTenantLine(),
            () -> sysTenantMapper.selectCount(new LambdaQueryWrapper<SysTenantEntity>()
                .eq(SysTenantEntity::getDeleted, 0)
                .eq(SysTenantEntity::getTenantId, tenantId))
        );
        if (count != null && count > 0L) {
            throw new BizException(409, "租户编号已存在，请重试");
        }
    }

    /**
     * 校验租户处于可用状态。
     *
     * @param id 租户主键编号
     * @return 租户实体
     */
    private SysTenantEntity requireActiveTenant(Long id) {
        return MybatisTenantHelpers.requireEntity(
            InterceptorIgnoreHelper.execute(
                MybatisTenantHelpers.ignoreTenantLine(),
                () -> sysTenantMapper.selectOne(new LambdaQueryWrapper<SysTenantEntity>()
                    .eq(SysTenantEntity::getDeleted, 0)
                    .eq(SysTenantEntity::getId, id)
                    .last("limit 1"))
            ),
            "租户不存在"
        );
    }

    /**
     * 解析日期字符串。
     *
     * @param value 日期字符串
     * @return 日期对象
     */
    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (Exception exception) {
            throw new BizException(400, "到期日期格式不正确");
        }
    }
}
