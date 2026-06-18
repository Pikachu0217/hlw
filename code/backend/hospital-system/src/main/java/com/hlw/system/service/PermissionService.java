package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreatePermissionReq;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.entity.SysPermissionEntity;
import com.hlw.system.mapper.SysMenuMapper;
import com.hlw.system.mapper.SysPermissionMapper;
import com.hlw.system.service.converter.PermissionConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.domain.resp.PermissionResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 权限码聚合服务，负责权限码的查询与创建编排，并解析菜单名称。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {
    private static final String DEFAULT_RESOURCE_TYPE = "按钮";

    /** 权限码数据访问组件。 */
    private final SysPermissionMapper sysPermissionMapper;
    /** 菜单数据访问组件，用于解析菜单名称。 */
    private final SysMenuMapper sysMenuMapper;
    /** 权限码展示对象转换器。 */
    private final PermissionConverter permissionConverter;

    /**
     * 分页查询权限码列表，并按当前页菜单编号补全菜单名称。
     *
     * @param query 分页查询条件
     * @return 权限码分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<PermissionResp> listPermissions(PageQuery query) {
        log.info("查询系统权限码列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());

        Page<SysPermissionEntity> page = query.toPage();
        LambdaQueryWrapper<SysPermissionEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysPermissionEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysPermissionEntity::getPermissionName, query.getKeyword());
        }
        wrapper.orderByAsc(SysPermissionEntity::getId);

        Page<SysPermissionEntity> result = sysPermissionMapper.selectPage(page, wrapper);
        Map<Long, String> menuMap = resolveMenuNameMap(result.getRecords());

        List<PermissionResp> records = result.getRecords().stream()
            .map(permission -> permissionConverter.toPermissionVO(permission, menuMap.get(permission.getMenuId())))
            .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 创建权限码。
     *
     * @param request 创建权限码请求
     * @return 新建权限码展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public PermissionResp createPermission(CreatePermissionReq request) {
        log.info("创建权限码，permissionName={}，permissionCode={}，resourceType={}",
            request.getPermissionName(), request.getPermissionCode(), request.getResourceType());
        String menuName = "-";
        if (request.getMenuId() != null) {
            menuName = requireActiveMenu(request.getMenuId()).getMenuName();
        }
        SysPermissionEntity entity = new SysPermissionEntity();
        entity.setPermissionName(request.getPermissionName());
        entity.setPermissionCode(request.getPermissionCode());
        entity.setResourceType(DefaultValueUtils.defaultIfBlank(request.getResourceType(), DEFAULT_RESOURCE_TYPE));
        entity.setMenuId(request.getMenuId());
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setDeleted(0);
        sysPermissionMapper.insert(entity);
        return permissionConverter.toPermissionVO(entity, menuName);
    }

    /**
     * 查询权限码详情。
     *
     * @param permissionId 权限码编号
     * @return 权限码展示对象
     */
    @Transactional(readOnly = true)
    public PermissionResp getPermission(Long permissionId) {
        log.info("查询系统权限码详情，permissionId={}", permissionId);
        SysPermissionEntity entity = requireActivePermission(permissionId);
        String menuName = entity.getMenuId() == null ? "-" : requireActiveMenu(entity.getMenuId()).getMenuName();
        return permissionConverter.toPermissionVO(entity, menuName);
    }

    /**
     * 更新权限码。
     *
     * @param permissionId 权限码编号
     * @param request 权限码更新请求
     * @return 更新后的权限码展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public PermissionResp updatePermission(Long permissionId, CreatePermissionReq request) {
        log.info("更新系统权限码，permissionId={}，permissionName={}，permissionCode={}",
            permissionId, request.getPermissionName(), request.getPermissionCode());
        String menuName = "-";
        if (request.getMenuId() != null) {
            menuName = requireActiveMenu(request.getMenuId()).getMenuName();
        }
        SysPermissionEntity entity = requireActivePermission(permissionId);
        entity.setPermissionName(request.getPermissionName());
        entity.setPermissionCode(request.getPermissionCode());
        entity.setResourceType(DefaultValueUtils.defaultIfBlank(request.getResourceType(), DEFAULT_RESOURCE_TYPE));
        entity.setMenuId(request.getMenuId());
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        sysPermissionMapper.updateById(entity);
        return permissionConverter.toPermissionVO(entity, menuName);
    }

    /**
     * 删除权限码。
     *
     * @param permissionId 权限码编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long permissionId) {
        log.info("删除系统权限码，permissionId={}", permissionId);
        SysPermissionEntity entity = requireActivePermission(permissionId);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        sysPermissionMapper.updateById(entity);
    }

    /**
     * 按当前页权限引用的菜单编号集合构建菜单名称映射。
     *
     * @param permissions 当前页权限列表
     * @return 菜单编号到菜单名称的映射
     */
    private Map<Long, String> resolveMenuNameMap(List<SysPermissionEntity> permissions) {
        Set<Long> menuIds = permissions.stream()
            .map(SysPermissionEntity::getMenuId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (menuIds.isEmpty()) {
            return Map.of();
        }
        return sysMenuMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysMenuEntity::getDeleted)
                .in(SysMenuEntity::getId, menuIds))
            .stream()
            .collect(Collectors.toMap(SysMenuEntity::getId, SysMenuEntity::getMenuName));
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

    /**
     * 校验权限码处于可用状态。
     *
     * @param permissionId 权限码编号
     * @return 权限码实体
     */
    private SysPermissionEntity requireActivePermission(Long permissionId) {
        return MybatisTenantHelpers.requireEntity(sysPermissionMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysPermissionEntity::getDeleted)
                .eq(SysPermissionEntity::getId, permissionId)
                .last("limit 1")), "权限码不存在");
    }
}
