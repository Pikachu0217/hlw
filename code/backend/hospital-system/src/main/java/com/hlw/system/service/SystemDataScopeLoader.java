package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.mybatis.datascope.context.DataScopeContext;
import com.hlw.common.mybatis.datascope.context.DataScopeContextHolder;
import com.hlw.common.mybatis.datascope.context.DataScopeLoader;
import com.hlw.common.mybatis.datascope.enums.DataScopeType;
import com.hlw.system.entity.SysRoleEntity;
import com.hlw.system.entity.SysUserRoleEntity;
import com.hlw.system.mapper.SysRoleMapper;
import com.hlw.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;

/**
 * 系统服务的数据权限上下文加载器。
 */
@Component
@RequiredArgsConstructor
public class SystemDataScopeLoader implements DataScopeLoader {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;

    @Override
    public DataScopeContext load(Long userId, Long tenantId) {
        if (userId == null) {
            return null;
        }
        return DataScopeContextHolder.supplyWithIgnore(() -> loadWithoutDataScope(userId));
    }

    private DataScopeContext loadWithoutDataScope(Long userId) {
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

        return DataScopeContext.builder()
                .userId(userId)
                .roleIds(roleIds)
                .deptIds(new LinkedHashSet<>())
                .effectiveType(effectiveType)
                .build();
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
