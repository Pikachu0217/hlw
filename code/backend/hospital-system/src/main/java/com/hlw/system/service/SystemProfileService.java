package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.tenant.TokenPrincipalContext;
import com.hlw.system.domain.resp.RouterResp;
import com.hlw.system.domain.resp.UserInfoResp;
import com.hlw.system.domain.resp.UserResp;
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
import com.hlw.system.service.converter.UserConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 当前登录用户资料与路由聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SystemProfileService {
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
    /** 菜单聚合服务。 */
    private final MenuService menuService;
    /** 用户展示对象转换器。 */
    private final UserConverter userConverter;

    /**
     * 查询当前登录用户信息。
     *
     * @return 当前登录用户信息
     */
    @Transactional(readOnly = true)
    public UserInfoResp getInfo() {
        Long loginUserId = TokenPrincipalContext.get().getUserId();
        log.info("查询当前登录用户信息，loginUserId={}", loginUserId);
        SysUserEntity user = requireLoginUser(loginUserId);
        List<SysRoleEntity> roles = loadRoles(user.getUserId());
        List<SysMenuEntity> menus = loadMenus(roles);
        UserResp userResp = userConverter.toUserVO(user, "-");
        UserInfoResp resp = new UserInfoResp();
        resp.setUser(userResp);
        resp.setRoles(roles.stream().map(SysRoleEntity::getRoleCode).distinct().toList());
        resp.setPermissions(menus.stream().map(SysMenuEntity::getPerms).filter(value -> value != null && !value.isBlank()).distinct().toList());
        return resp;
    }

    /**
     * 查询当前登录用户路由树。
     *
     * @return 前端路由树
     */
    @Transactional(readOnly = true)
    public List<RouterResp> getRouters() {
        Long loginUserId = TokenPrincipalContext.get().getUserId();
        log.info("查询当前登录用户路由，loginUserId={}", loginUserId);
        SysUserEntity user = requireLoginUser(loginUserId);
        return menuService.buildRouters(loadMenus(loadRoles(user.getUserId())));
    }

    /**
     * 查询当前登录用户实体。
     *
     * @param loginUserId 登录用户表主键
     * @return 用户实体
     */
    private SysUserEntity requireLoginUser(Long loginUserId) {
        return MybatisTenantHelpers.requireEntity(sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getId, loginUserId)
                .last("limit 1")), "登录用户不存在");
    }

    /**
     * 加载用户角色。
     *
     * @param userId 用户业务编号
     * @return 角色列表
     */
    private List<SysRoleEntity> loadRoles(String userId) {
        List<Long> roleIds = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getUserId, userId)).stream()
            .map(SysUserRoleEntity::getRoleId)
            .distinct()
            .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return sysRoleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>()
            .eq(SysRoleEntity::getStatus, 0)
            .in(SysRoleEntity::getId, roleIds));
    }

    /**
     * 加载角色菜单。
     *
     * @param roles 角色列表
     * @return 菜单列表
     */
    private List<SysMenuEntity> loadMenus(List<SysRoleEntity> roles) {
        Set<Long> roleIds = roles.stream().map(SysRoleEntity::getId).collect(Collectors.toSet());
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenuEntity>()
                .in(SysRoleMenuEntity::getRoleId, roleIds)).stream()
            .map(SysRoleMenuEntity::getMenuId)
            .distinct()
            .toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getStatus, "0")
            .in(SysMenuEntity::getId, menuIds));
    }
}
