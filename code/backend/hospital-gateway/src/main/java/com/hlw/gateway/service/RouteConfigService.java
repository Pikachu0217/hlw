package com.hlw.gateway.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.constants.CommonConstants;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.gateway.domain.req.CreateRouteConfigReq;
import com.hlw.gateway.domain.req.UpdateRouteConfigReq;
import com.hlw.gateway.domain.resp.RouteConfigResp;
import com.hlw.gateway.entity.GwRouteConfigEntity;
import com.hlw.gateway.mapper.GwRouteConfigMapper;
import com.hlw.gateway.service.converter.RouteConfigConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 网关路由配置聚合服务，负责管理端维护路由配置清单。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RouteConfigService {
    /** 网关路由配置数据访问组件。 */
    private final GwRouteConfigMapper gwRouteConfigMapper;
    /** 网关路由配置展示对象转换器。 */
    private final RouteConfigConverter routeConfigConverter;

    /**
     * 分页查询网关路由配置。
     *
     * @param query 分页查询条件
     * @return 网关路由配置分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<RouteConfigResp> listRoutes(PageQuery query) {
        log.info("查询网关路由配置分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        Page<GwRouteConfigEntity> page = query.toPage();
        LambdaQueryWrapper<GwRouteConfigEntity> wrapper = activeWrapper();
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword();
            wrapper.and(w -> w.like(GwRouteConfigEntity::getRouteCode, keyword)
                .or()
                .like(GwRouteConfigEntity::getPathPredicate, keyword));
        }
        wrapper.orderByAsc(GwRouteConfigEntity::getSort).orderByAsc(GwRouteConfigEntity::getId);
        Page<GwRouteConfigEntity> result = ignoreTenantLine(() -> gwRouteConfigMapper.selectPage(page, wrapper));
        List<RouteConfigResp> records = result.getRecords().stream()
            .map(routeConfigConverter::toResp)
            .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 创建网关路由配置。
     *
     * @param request 创建路由配置请求
     * @return 新建路由配置展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public RouteConfigResp createRoute(CreateRouteConfigReq request) {
        log.info("创建网关路由配置，routeCode={}，uri={}，pathPredicate={}",
            request.getRouteCode(), request.getUri(), request.getPathPredicate());
        GwRouteConfigEntity entity = new GwRouteConfigEntity();
        entity.setTenantId(String.valueOf(CommonConstants.PLATFORM_TENANT_ID));
        entity.setRouteCode(request.getRouteCode());
        entity.setUri(request.getUri());
        entity.setPathPredicate(request.getPathPredicate());
        entity.setSort(DefaultValueUtils.defaultIfNull(request.getSort(), 0));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setRemark(DefaultValueUtils.defaultIfBlank(request.getRemark(), ""));
        ignoreTenantLine(() -> gwRouteConfigMapper.insert(entity));
        return routeConfigConverter.toResp(entity);
    }

    /**
     * 查询网关路由配置详情。
     *
     * @param routeId 路由配置编号
     * @return 路由配置展示对象
     */
    @Transactional(readOnly = true)
    public RouteConfigResp getRoute(Long routeId) {
        log.info("查询网关路由配置详情，routeId={}", routeId);
        return routeConfigConverter.toResp(requireActiveRoute(routeId));
    }

    /**
     * 更新网关路由配置。
     *
     * @param routeId 路由配置编号
     * @param request 更新路由配置请求
     * @return 更新后的路由配置展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public RouteConfigResp updateRoute(Long routeId, UpdateRouteConfigReq request) {
        log.info("更新网关路由配置，routeId={}，routeCode={}，uri={}", routeId, request.getRouteCode(), request.getUri());
        GwRouteConfigEntity entity = requireActiveRoute(routeId);
        entity.setRouteCode(request.getRouteCode());
        entity.setUri(request.getUri());
        entity.setPathPredicate(request.getPathPredicate());
        entity.setSort(DefaultValueUtils.defaultIfNull(request.getSort(), 0));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setRemark(DefaultValueUtils.defaultIfBlank(request.getRemark(), ""));
        ignoreTenantLine(() -> gwRouteConfigMapper.updateById(entity));
        return routeConfigConverter.toResp(entity);
    }

    /**
     * 删除网关路由配置。
     *
     * @param routeId 路由配置编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoute(Long routeId) {
        log.info("删除网关路由配置，routeId={}", routeId);
        requireActiveRoute(routeId);
        ignoreTenantLine(() -> gwRouteConfigMapper.deleteById(routeId));
    }

    /**
     * 构造未删除路由配置查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<GwRouteConfigEntity> activeWrapper() {
        return new LambdaQueryWrapper<GwRouteConfigEntity>();
    }

    /**
     * 校验路由配置处于可用状态。
     *
     * @param routeId 路由配置编号
     * @return 路由配置实体
     */
    private GwRouteConfigEntity requireActiveRoute(Long routeId) {
        GwRouteConfigEntity entity = ignoreTenantLine(() -> gwRouteConfigMapper.selectOne(activeWrapper()
            .eq(GwRouteConfigEntity::getId, routeId)
            .last("limit 1")));
        if (entity == null) {
            throw new BizException(404, "网关路由配置不存在");
        }
        return entity;
    }

    /**
     * 在平台路由配置读写时忽略租户行过滤。
     *
     * @param supplier 数据库操作
     * @param <T> 返回类型
     * @return 操作结果
     */
    private <T> T ignoreTenantLine(java.util.function.Supplier<T> supplier) {
        return InterceptorIgnoreHelper.execute(IgnoreStrategy.builder().tenantLine(true).build(), supplier);
    }
}
