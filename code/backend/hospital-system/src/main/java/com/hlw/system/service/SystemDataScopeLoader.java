package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.mybatis.datascope.context.DataScopeContext;
import com.hlw.common.mybatis.datascope.context.DataScopeContextHolder;
import com.hlw.common.mybatis.datascope.context.DataScopeLoader;
import com.hlw.common.mybatis.datascope.enums.DataScopeType;
import com.hlw.system.entity.SysDeptEntity;
import com.hlw.system.entity.SysRoleEntity;
import com.hlw.system.entity.SysUserEntity;
import com.hlw.system.entity.SysUserRoleEntity;
import com.hlw.system.mapper.SysDeptMapper;
import com.hlw.system.mapper.SysRoleMapper;
import com.hlw.system.mapper.SysUserMapper;
import com.hlw.system.mapper.SysUserRoleMapper;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 系统服务的数据权限上下文加载器。
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SystemDataScopeLoader implements DataScopeLoader {
    /** 用户数据访问组件。 */
    private final SysUserMapper sysUserMapper;
    /** 用户角色关系数据访问组件。 */
    private final SysUserRoleMapper sysUserRoleMapper;
    /** 角色数据访问组件。 */
    private final SysRoleMapper sysRoleMapper;
    /** 部门数据访问组件。 */
    private final SysDeptMapper sysDeptMapper;

    /**
     * 加载数据权限上下文。
     *
     * @param userId 登录用户表主键
     * @param tenantId 登录租户编号
     * @return 数据权限上下文
     */
    @Override
    public DataScopeContext load(Long userId, Long tenantId) {
        if (userId == null) {
            return null;
        }
        return DataScopeContextHolder.supplyWithIgnore(() -> loadWithoutDataScope(userId, tenantId));
    }

    /**
     * 在忽略数据权限的上下文中加载权限范围。
     *
     * @param userId 登录用户表主键
     * @param tenantId 登录租户编号
     * @return 数据权限上下文
     */
    private DataScopeContext loadWithoutDataScope(Long userId, Long tenantId) {
        SysUserEntity user = loadUser(userId, tenantId);
        if (user == null) {
            log.warn("加载数据权限失败，用户不存在，userId={}，tenantId={}", userId, tenantId);
            return DataScopeContext.builder().userId(userId).effectiveType(DataScopeType.SELF).build();
        }
        List<Long> roleIds = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getUserId, user.getUserId())).stream()
            .map(SysUserRoleEntity::getRoleId)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
        if (roleIds.isEmpty()) {
            return DataScopeContext.builder().userId(userId).deptId(user.getDeptId()).effectiveType(DataScopeType.SELF).build();
        }
        List<SysRoleEntity> roles = sysRoleMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysRoleEntity::getDeleted)
            .eq(SysRoleEntity::getStatus, 0)
            .in(SysRoleEntity::getId, roleIds));
        DataScopeType effectiveType = roles.stream()
            .map(SysRoleEntity::getDataScope)
            .map(this::fromSchemaCode)
            .filter(Objects::nonNull)
            .min(Comparator.comparingInt(this::priority))
            .orElse(DataScopeType.SELF);
        Set<Long> deptIds = resolveDeptIds(user.getDeptId(), tenantId, effectiveType);
        return DataScopeContext.builder()
            .userId(userId)
            .deptId(user.getDeptId())
            .roleIds(roleIds)
            .deptIds(deptIds)
            .effectiveType(effectiveType)
            .build();
    }

    /**
     * 加载用户实体。
     *
     * @param userId 用户表主键
     * @param tenantId 租户编号
     * @return 用户实体
     */
    private SysUserEntity loadUser(Long userId, Long tenantId) {
        LambdaQueryWrapper<SysUserEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysUserEntity::getDeleted)
            .eq(SysUserEntity::getId, userId);
        if (tenantId != null) {
            wrapper.eq(SysUserEntity::getTenantId, String.valueOf(tenantId));
        }
        return sysUserMapper.selectOne(wrapper.last("limit 1"));
    }

    /**
     * 根据 schema 数据权限编码解析权限类型。
     *
     * @param code schema 中的 data_scope 编码
     * @return 数据权限类型
     */
    private DataScopeType fromSchemaCode(Integer code) {
        if (code == null) {
            return null;
        }
        return switch (code) {
            case 1 -> DataScopeType.ALL;
            case 2 -> DataScopeType.CUSTOM;
            case 3 -> DataScopeType.DEPT;
            case 4 -> DataScopeType.DEPT_AND_CHILD;
            case 5 -> DataScopeType.SELF;
            case 6 -> DataScopeType.DEPT_AND_CHILD;
            default -> null;
        };
    }

    /**
     * 解析部门权限集合。
     *
     * @param deptId 当前部门编号
     * @param tenantId 当前租户编号
     * @param type 数据权限类型
     * @return 部门编号集合
     */
    private Set<Long> resolveDeptIds(Long deptId, Long tenantId, DataScopeType type) {
        if (deptId == null) {
            return new LinkedHashSet<>();
        }
        if (type != DataScopeType.DEPT_AND_CHILD) {
            LinkedHashSet<Long> deptIds = new LinkedHashSet<>();
            deptIds.add(deptId);
            return deptIds;
        }
        List<SysDeptEntity> depts = loadActiveDepts(tenantId);
        LinkedHashSet<Long> deptIds = new LinkedHashSet<>();
        deptIds.add(deptId);
        Deque<Long> queue = new ArrayDeque<>();
        queue.add(deptId);
        while (!queue.isEmpty()) {
            Long current = queue.removeFirst();
            for (SysDeptEntity dept : depts) {
                if (Objects.equals(dept.getParentId(), current) && deptIds.add(dept.getId())) {
                    queue.addLast(dept.getId());
                }
            }
        }
        return deptIds;
    }

    /**
     * 加载启用部门列表。
     *
     * @param tenantId 租户编号
     * @return 部门列表
     */
    private List<SysDeptEntity> loadActiveDepts(Long tenantId) {
        LambdaQueryWrapper<SysDeptEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysDeptEntity::getDeleted)
            .eq(SysDeptEntity::getStatus, 0);
        if (tenantId != null) {
            wrapper.eq(SysDeptEntity::getTenantId, String.valueOf(tenantId));
        }
        return sysDeptMapper.selectList(wrapper);
    }

    /**
     * 数据权限优先级，数值越小权限越大。
     *
     * @param type 数据权限类型
     * @return 优先级
     */
    private int priority(DataScopeType type) {
        return switch (type) {
            case ALL -> 0;
            case TENANT -> 1;
            case DEPT_AND_CHILD -> 2;
            case DEPT -> 3;
            case SELF -> 4;
            case CUSTOM -> 5;
            case ANNOTATION_INHERIT -> 6;
        };
    }
}
