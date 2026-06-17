package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.dto.CreatePermissionRequest;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.entity.SysPermissionEntity;
import com.hlw.system.mapper.SysMenuMapper;
import com.hlw.system.mapper.SysPermissionMapper;
import com.hlw.system.service.converter.PermissionConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.vo.PermissionVO;
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
    public PageResult<PermissionVO> listPermissions(PageQuery query) {
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

        List<PermissionVO> records = result.getRecords().stream()
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
    public PermissionVO createPermission(CreatePermissionRequest request) {
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
}
