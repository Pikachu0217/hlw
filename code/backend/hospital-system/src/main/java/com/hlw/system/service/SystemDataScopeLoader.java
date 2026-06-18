package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.enums.CommonStatusEnum;
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
public class SystemDataScopeLoader implements DataScopeLoader {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysDeptMapper sysDeptMapper;

    @Override
    public DataScopeContext load(Long userId, Long tenantId) {
        if (userId == null) {
            return null;
        }
        return DataScopeContextHolder.supplyWithIgnore(() -> loadWithoutDataScope(userId, tenantId));
    }

    private DataScopeContext loadWithoutDataScope(Long userId, Long tenantId) {
        SysUserEntity user = loadUser(userId, tenantId);
        List<SysUserRoleEntity> relations = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getDeleted, 0)
                .eq(SysUserRoleEntity::getStatus, CommonStatusEnum.ENABLED.getStatus())
                .eq(SysUserRoleEntity::getUserId, userId));
        List<Long> roleIds = relations.stream()
                .map(SysUserRoleEntity::getRoleId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (roleIds.isEmpty()) {
            return DataScopeContext.builder()
                    .userId(userId)
                    .deptId(user == null ? null : user.getDeptId())
                    .effectiveType(DataScopeType.SELF)
                    .build();
        }

        List<SysRoleEntity> roles = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getDeleted, 0)
                .eq(SysRoleEntity::getStatus, CommonStatusEnum.ENABLED.getStatus())
                .in(SysRoleEntity::getId, roleIds));
        DataScopeType effectiveType = roles.stream()
                .map(SysRoleEntity::getDataScope)
                .map(DataScopeType::fromCode)
                .filter(Objects::nonNull)
                .min(Comparator.comparingInt(this::priority))
                .orElse(DataScopeType.SELF);

        Set<Long> deptIds = resolveDeptIds(user == null ? null : user.getDeptId(), tenantId, effectiveType);
        return DataScopeContext.builder()
                .userId(userId)
                .deptId(user == null ? null : user.getDeptId())
                .roleIds(roleIds)
                .deptIds(deptIds)
                .effectiveType(effectiveType)
                .build();
    }

    private SysUserEntity loadUser(Long userId, Long tenantId) {
        LambdaQueryWrapper<SysUserEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysUserEntity::getDeleted)
                .eq(SysUserEntity::getId, userId)
                .last("limit 1");
        if (tenantId != null) {
            wrapper.eq(SysUserEntity::getTenantId, tenantId);
        }
        return sysUserMapper.selectOne(wrapper);
    }

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
        if (depts.isEmpty()) {
            LinkedHashSet<Long> deptIds = new LinkedHashSet<>();
            deptIds.add(deptId);
            return deptIds;
        }
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

    private List<SysDeptEntity> loadActiveDepts(Long tenantId) {
        LambdaQueryWrapper<SysDeptEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysDeptEntity::getDeleted)
                .eq(SysDeptEntity::getStatus, CommonStatusEnum.ENABLED.getStatus());
        if (tenantId != null) {
            wrapper.eq(SysDeptEntity::getTenantId, tenantId);
        }
        return sysDeptMapper.selectList(wrapper);
    }

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
