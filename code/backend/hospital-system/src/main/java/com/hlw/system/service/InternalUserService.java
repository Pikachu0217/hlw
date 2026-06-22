package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.hlw.common.core.domain.system.resp.InternalUserResp;
import com.hlw.system.constants.SystemTenantConstants;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.entity.SysRoleEntity;
import com.hlw.system.entity.SysRoleMenuEntity;
import com.hlw.system.entity.SysUserEntity;
import com.hlw.system.entity.SysUserRoleEntity;
import com.hlw.system.mapper.SysMenuMapper;
import com.hlw.system.mapper.SysRoleMapper;
import com.hlw.system.mapper.SysRoleMenuMapper;
import com.hlw.system.mapper.SysUserMapper;
import com.hlw.system.mapper.SysUserRoleMapper;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 系统内部用户查询服务，供认证服务通过 Feign 调用。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InternalUserService {
    /** 用户数据访问组件。 */
    private final SysUserMapper sysUserMapper;
    /** 用户角色关系数据访问组件。 */
    private final SysUserRoleMapper sysUserRoleMapper;
    /** 角色数据访问组件。 */
    private final SysRoleMapper sysRoleMapper;
    /** 角色菜单关系数据访问组件。 */
    private final SysRoleMenuMapper sysRoleMenuMapper;
    /** 菜单数据访问组件。 */
    private final SysMenuMapper sysMenuMapper;

    /**
     * 按租户编号和登录账号查询用户。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @return 内部用户展示对象
     */
    @Transactional(readOnly = true)
    public InternalUserResp findByTenantIdAndUsername(Long tenantId, String username) {
        log.info("内部查询用户，tenantId={}，username={}", tenantId, username);
        SysUserEntity entity = ignoreTenantLine(() -> sysUserMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getTenantId, String.valueOf(tenantId))
            .eq(SysUserEntity::getUserName, username)
            .last("limit 1")));
        return entity == null ? null : toInternalUserResp(entity);
    }

    /**
     * 按用户表主键和租户编号查询用户。
     *
     * @param id 用户表主键
     * @param tenantId 租户编号
     * @return 内部用户展示对象
     */
    @Transactional(readOnly = true)
    public InternalUserResp findByIdAndTenantId(Long id, Long tenantId) {
        log.info("内部查询用户资料，id={}，tenantId={}", id, tenantId);
        SysUserEntity entity = ignoreTenantLine(() -> sysUserMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getId, id)
            .eq(SysUserEntity::getTenantId, String.valueOf(tenantId))
            .last("limit 1")));
        return entity == null ? null : toInternalUserResp(entity);
    }

    /**
     * 转换内部用户展示对象。
     *
     * @param entity 用户实体
     * @return 内部用户展示对象
     */
    private InternalUserResp toInternalUserResp(SysUserEntity entity) {
        List<SysRoleEntity> roles = loadRoles(entity.getUserId(), entity.getTenantId());
        Set<Long> roleIds = roles.stream().map(SysRoleEntity::getId).collect(Collectors.toSet());
        List<String> perms = loadPerms(roleIds, entity.getTenantId());
        InternalUserResp resp = new InternalUserResp();
        resp.setId(entity.getId());
        resp.setUserId(entity.getUserId());
        resp.setTenantId(parseTenantId(entity.getTenantId()));
        resp.setTenantCode(entity.getTenantId());
        resp.setUsername(entity.getUserName());
        resp.setRealName(entity.getRealName());
        resp.setPassword(entity.getPassword());
        resp.setPhone(entity.getPhone());
        resp.setUserType(entity.getUserType());
        resp.setRoleCode(roles.stream().map(SysRoleEntity::getRoleCode).findFirst().orElse(""));
        resp.setResourceRoutePathList(perms);
        resp.setStatus(String.valueOf(entity.getStatus()));
        return resp;
    }

    /**
     * 加载用户角色。
     *
     * @param userId 用户业务编号
     * @param tenantId 租户编号
     * @return 角色列表
     */
    private List<SysRoleEntity> loadRoles(String userId, String tenantId) {
        List<Long> roleIds = ignoreTenantLine(() -> sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getTenantId, tenantId)
                .eq(SysUserRoleEntity::getUserId, userId))).stream()
            .map(SysUserRoleEntity::getRoleId)
            .distinct()
            .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return ignoreTenantLine(() -> sysRoleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>()
            .eq(SysRoleEntity::getTenantId, tenantId)
            .eq(SysRoleEntity::getStatus, SystemTenantConstants.STATUS_NORMAL_VALUE)
            .in(SysRoleEntity::getId, roleIds)));
    }

    /**
     * 加载角色权限标识。
     *
     * @param roleIds 角色编号集合
     * @param tenantId 租户编号
     * @return 权限标识列表
     */
    private List<String> loadPerms(Set<Long> roleIds, String tenantId) {
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = ignoreTenantLine(() -> sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenuEntity>()
                .eq(SysRoleMenuEntity::getTenantId, tenantId)
                .in(SysRoleMenuEntity::getRoleId, roleIds))).stream()
            .map(SysRoleMenuEntity::getMenuId)
            .distinct()
            .toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return ignoreTenantLine(() -> sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
                .eq(SysMenuEntity::getTenantId, tenantId)
                .eq(SysMenuEntity::getStatus, SystemTenantConstants.STATUS_NORMAL)
                .in(SysMenuEntity::getId, menuIds))).stream()
            .map(SysMenuEntity::getPerms)
            .filter(value -> value != null && !value.isBlank())
            .distinct()
            .toList();
    }

    /**
     * 解析租户编号。
     *
     * @param tenantId 租户编号字符串
     * @return Long 租户编号
     */
    private Long parseTenantId(String tenantId) {
        try {
            return Long.parseLong(tenantId);
        } catch (NumberFormatException exception) {
            log.warn("租户编号无法转换为 Long，tenantId={}", tenantId);
            return -1L;
        }
    }

    /**
     * 执行显式租户条件查询并跳过 MyBatis Plus 自动租户拼接。
     *
     * @param supplier 查询执行器
     * @param <T> 返回值类型
     * @return 查询返回值
     */
    private <T> T ignoreTenantLine(Supplier<T> supplier) {
        return InterceptorIgnoreHelper.execute(MybatisTenantHelpers.ignoreTenantLine(), supplier);
    }
}
