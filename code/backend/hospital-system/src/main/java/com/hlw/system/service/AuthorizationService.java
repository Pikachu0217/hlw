package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.system.dto.BindRoleMenuRequest;
import com.hlw.system.dto.BindUserRoleRequest;
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
import com.hlw.system.service.converter.AuthorizationConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.vo.RelationBindingVO;
import com.hlw.system.vo.RoleMenuVO;
import com.hlw.system.vo.UserRoleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

/**
 * 授权聚合服务，负责用户角色与角色菜单两类绑定关系的查询、绑定及成员数维护。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {
    /** 用户角色关联数据访问组件。 */
    private final SysUserRoleMapper sysUserRoleMapper;
    /** 角色菜单关联数据访问组件。 */
    private final SysRoleMenuMapper sysRoleMenuMapper;
    /** 角色数据访问组件，用于成员数刷新及角色名展示。 */
    private final SysRoleMapper sysRoleMapper;
    /** 用户数据访问组件，用于绑定校验及用户名展示。 */
    private final SysUserMapper sysUserMapper;
    /** 菜单数据访问组件，用于绑定校验及菜单信息展示。 */
    private final SysMenuMapper sysMenuMapper;
    /** 授权关系展示对象转换器。 */
    private final AuthorizationConverter authorizationConverter;

    /**
     * 查询用户角色授权列表。
     *
     * @return 用户角色授权展示列表
     */
    @Transactional(readOnly = true)
    public List<UserRoleVO> listUserRoles() {
        log.info("查询用户角色授权列表");
        Map<Long, String> userMap = sysUserMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysUserEntity::getDeleted))
            .stream()
            .collect(Collectors.toMap(SysUserEntity::getId, SysUserEntity::getUsername));
        Map<Long, String> roleMap = sysRoleMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysRoleEntity::getDeleted))
            .stream()
            .collect(Collectors.toMap(SysRoleEntity::getId, SysRoleEntity::getRoleName));
        return sysUserRoleMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysUserRoleEntity::getDeleted))
            .stream()
            .sorted(Comparator.comparing(SysUserRoleEntity::getId))
            .map(relation -> {
                UserRoleVO vo = new UserRoleVO();
                vo.setKey(String.valueOf(relation.getId()));
                vo.setUsername(userMap.getOrDefault(relation.getUserId(), "-"));
                vo.setRoleName(roleMap.getOrDefault(relation.getRoleId(), "-"));
                vo.setStatus(relation.getStatus());
                return vo;
            })
            .toList();
    }

    /**
     * 绑定用户角色。
     *
     * @param request 绑定用户角色请求
     * @return 绑定展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public RelationBindingVO bindUserRole(BindUserRoleRequest request) {
        log.info("绑定用户角色，userId={}，roleId={}", request.getUserId(), request.getRoleId());
        requireActiveUser(request.getUserId());
        requireActiveRole(request.getRoleId());
        SysUserRoleEntity existed = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRoleEntity>()
            .eq(SysUserRoleEntity::getDeleted, 0)
            .eq(SysUserRoleEntity::getUserId, request.getUserId())
            .eq(SysUserRoleEntity::getRoleId, request.getRoleId())
            .last("limit 1"));
        if (existed != null) {
            log.info("用户角色已绑定，userId={}，roleId={}", request.getUserId(), request.getRoleId());
            return authorizationConverter.toRelationBindingVO(existed, request.getUserId(), request.getRoleId());
        }
        SysUserRoleEntity entity = new SysUserRoleEntity();
        entity.setUserId(request.getUserId());
        entity.setRoleId(request.getRoleId());
        entity.setStatus(CommonStatusEnum.ENABLED.getStatus());
        entity.setDeleted(0);
        sysUserRoleMapper.insert(entity);
        refreshRoleMemberCount(request.getRoleId());
        return authorizationConverter.toRelationBindingVO(entity, request.getUserId(), request.getRoleId());
    }

    /**
     * 查询角色菜单授权列表。
     *
     * @return 角色菜单授权展示列表
     */
    @Transactional(readOnly = true)
    public List<RoleMenuVO> listRoleMenus() {
        log.info("查询角色菜单授权列表");
        Map<Long, String> roleMap = sysRoleMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysRoleEntity::getDeleted))
            .stream()
            .collect(Collectors.toMap(SysRoleEntity::getId, SysRoleEntity::getRoleName));
        Map<Long, SysMenuEntity> menuMap = sysMenuMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysMenuEntity::getDeleted))
            .stream()
            .collect(Collectors.toMap(SysMenuEntity::getId, menu -> menu));
        return sysRoleMenuMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysRoleMenuEntity::getDeleted))
            .stream()
            .sorted(Comparator.comparing(SysRoleMenuEntity::getId))
            .map(relation -> {
                RoleMenuVO vo = new RoleMenuVO();
                vo.setKey(String.valueOf(relation.getId()));
                vo.setRoleName(roleMap.getOrDefault(relation.getRoleId(), "-"));
                SysMenuEntity menu = menuMap.get(relation.getMenuId());
                vo.setMenuName(menu == null ? "-" : menu.getMenuName());
                vo.setPermission(menu == null ? "-" : menu.getPermission());
                vo.setStatus(relation.getStatus());
                return vo;
            })
            .toList();
    }

    /**
     * 绑定角色菜单。
     *
     * @param request 绑定角色菜单请求
     * @return 绑定展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public RelationBindingVO bindRoleMenu(BindRoleMenuRequest request) {
        log.info("绑定角色菜单，roleId={}，menuId={}", request.getRoleId(), request.getMenuId());
        requireActiveRole(request.getRoleId());
        requireActiveMenu(request.getMenuId());
        SysRoleMenuEntity existed = sysRoleMenuMapper.selectOne(new LambdaQueryWrapper<SysRoleMenuEntity>()
            .eq(SysRoleMenuEntity::getDeleted, 0)
            .eq(SysRoleMenuEntity::getRoleId, request.getRoleId())
            .eq(SysRoleMenuEntity::getMenuId, request.getMenuId())
            .last("limit 1"));
        if (existed != null) {
            log.info("角色菜单已绑定，roleId={}，menuId={}", request.getRoleId(), request.getMenuId());
            return authorizationConverter.toRelationBindingVO(existed, request.getRoleId(), request.getMenuId());
        }
        SysRoleMenuEntity entity = new SysRoleMenuEntity();
        entity.setRoleId(request.getRoleId());
        entity.setMenuId(request.getMenuId());
        entity.setStatus(CommonStatusEnum.ENABLED.getStatus());
        entity.setDeleted(0);
        sysRoleMenuMapper.insert(entity);
        return authorizationConverter.toRelationBindingVO(entity, request.getRoleId(), request.getMenuId());
    }

    /**
     * 刷新角色成员数量，直接通过 SysRoleMapper 写回以避免与 RoleService 形成循环依赖。
     *
     * @param roleId 角色编号
     */
    private void refreshRoleMemberCount(Long roleId) {
        SysRoleEntity role = requireActiveRole(roleId);
        int count = Math.toIntExact(sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRoleEntity>()
            .eq(SysUserRoleEntity::getDeleted, 0)
            .eq(SysUserRoleEntity::getRoleId, roleId)));
        role.setMemberCount(count);
        sysRoleMapper.updateById(role);
    }

    /**
     * 校验用户处于可用状态，直接通过 SysUserMapper 查询以避免跨服务依赖。
     *
     * @param userId 用户编号
     * @return 用户实体
     */
    private SysUserEntity requireActiveUser(Long userId) {
        return MybatisTenantHelpers.requireEntity(sysUserMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getDeleted, 0)
            .eq(SysUserEntity::getId, userId)
            .last("limit 1")), "用户不存在");
    }

    /**
     * 校验角色处于可用状态。
     *
     * @param roleId 角色编号
     * @return 角色实体
     */
    private SysRoleEntity requireActiveRole(Long roleId) {
        return MybatisTenantHelpers.requireEntity(sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRoleEntity>()
            .eq(SysRoleEntity::getDeleted, 0)
            .eq(SysRoleEntity::getId, roleId)
            .last("limit 1")), "角色不存在");
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
