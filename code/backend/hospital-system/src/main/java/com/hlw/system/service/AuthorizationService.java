package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.system.domain.req.BindRoleMenuReq;
import com.hlw.system.domain.req.BindUserRoleReq;
import com.hlw.system.domain.resp.RelationBindingResp;
import com.hlw.system.domain.resp.RoleMenuResp;
import com.hlw.system.domain.resp.UserRoleResp;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 授权聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {
    /** 用户角色关联数据访问组件。 */
    private final SysUserRoleMapper sysUserRoleMapper;
    /** 角色菜单关联数据访问组件。 */
    private final SysRoleMenuMapper sysRoleMenuMapper;
    /** 用户数据访问组件。 */
    private final SysUserMapper sysUserMapper;
    /** 角色数据访问组件。 */
    private final SysRoleMapper sysRoleMapper;
    /** 菜单数据访问组件。 */
    private final SysMenuMapper sysMenuMapper;
    /** 授权关系展示对象转换器。 */
    private final AuthorizationConverter authorizationConverter;

    /**
     * 查询用户角色授权列表。
     *
     * @return 用户角色授权列表
     */
    @Transactional(readOnly = true)
    public java.util.List<UserRoleResp> listUserRoles() {
        log.info("查询用户角色授权列表");
        Map<String, SysUserEntity> userMap = sysUserMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysUserEntity::getDeleted))
            .stream()
            .collect(Collectors.toMap(SysUserEntity::getUserId, user -> user, (left, right) -> left));
        Map<Long, String> roleMap = sysRoleMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysRoleEntity::getDeleted))
            .stream()
            .collect(Collectors.toMap(SysRoleEntity::getId, SysRoleEntity::getRoleName, (left, right) -> left));
        return sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>())
            .stream()
            .sorted(Comparator.comparing(SysUserRoleEntity::getId))
            .map(relation -> {
                UserRoleResp resp = new UserRoleResp();
                SysUserEntity user = userMap.get(relation.getUserId());
                resp.setKey(String.valueOf(relation.getId()));
                resp.setUserId(relation.getUserId());
                resp.setUserName(user == null ? "-" : user.getUserName());
                resp.setRoleName(roleMap.getOrDefault(relation.getRoleId(), "-"));
                return resp;
            })
            .toList();
    }

    /**
     * 查询用户角色授权详情。
     *
     * @param relationId 关系编号
     * @return 用户角色授权详情
     */
    @Transactional(readOnly = true)
    public UserRoleResp getUserRole(Long relationId) {
        log.info("查询用户角色授权详情，relationId={}", relationId);
        SysUserRoleEntity relation = requireUserRole(relationId);
        SysUserEntity user = requireUser(relation.getUserId());
        SysRoleEntity role = requireRole(relation.getRoleId());
        UserRoleResp resp = new UserRoleResp();
        resp.setKey(String.valueOf(relation.getId()));
        resp.setUserId(relation.getUserId());
        resp.setUserName(user.getUserName());
        resp.setRoleName(role.getRoleName());
        return resp;
    }

    /**
     * 绑定用户角色。
     *
     * @param request 用户角色绑定请求
     * @return 绑定结果
     */
    @Transactional(rollbackFor = Exception.class)
    public RelationBindingResp bindUserRole(BindUserRoleReq request) {
        log.info("绑定用户角色，userId={}，roleId={}", request.getUserId(), request.getRoleId());
        requireUser(request.getUserId());
        requireRole(request.getRoleId());
        SysUserRoleEntity existed = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRoleEntity>()
            .eq(SysUserRoleEntity::getUserId, request.getUserId())
            .eq(SysUserRoleEntity::getRoleId, request.getRoleId())
            .last("limit 1"));
        if (existed != null) {
            log.info("用户角色关系已存在，userId={}，roleId={}", request.getUserId(), request.getRoleId());
            return authorizationConverter.toRelationBindingVO(existed, request.getUserId(), request.getRoleId());
        }
        SysUserRoleEntity entity = new SysUserRoleEntity();
        entity.setUserId(request.getUserId());
        entity.setRoleId(request.getRoleId());
        sysUserRoleMapper.insert(entity);
        return authorizationConverter.toRelationBindingVO(entity, request.getUserId(), request.getRoleId());
    }

    /**
     * 删除用户角色授权。
     *
     * @param relationId 关系编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserRole(Long relationId) {
        log.info("删除用户角色授权，relationId={}", relationId);
        requireUserRole(relationId);
        sysUserRoleMapper.deleteById(relationId);
    }

    /**
     * 查询角色菜单授权列表。
     *
     * @return 角色菜单授权列表
     */
    @Transactional(readOnly = true)
    public java.util.List<RoleMenuResp> listRoleMenus() {
        log.info("查询角色菜单授权列表");
        Map<Long, String> roleMap = sysRoleMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysRoleEntity::getDeleted))
            .stream()
            .collect(Collectors.toMap(SysRoleEntity::getId, SysRoleEntity::getRoleName, (left, right) -> left));
        Map<Long, SysMenuEntity> menuMap = sysMenuMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysMenuEntity::getDeleted))
            .stream()
            .collect(Collectors.toMap(SysMenuEntity::getId, menu -> menu, (left, right) -> left));
        return sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenuEntity>())
            .stream()
            .sorted(Comparator.comparing(SysRoleMenuEntity::getId))
            .map(relation -> {
                RoleMenuResp resp = new RoleMenuResp();
                SysMenuEntity menu = menuMap.get(relation.getMenuId());
                resp.setKey(String.valueOf(relation.getId()));
                resp.setRoleName(roleMap.getOrDefault(relation.getRoleId(), "-"));
                resp.setMenuName(menu == null ? "-" : menu.getMenuName());
                resp.setPerms(menu == null ? "-" : menu.getPerms());
                return resp;
            })
            .toList();
    }

    /**
     * 查询角色菜单授权详情。
     *
     * @param relationId 关系编号
     * @return 角色菜单授权详情
     */
    @Transactional(readOnly = true)
    public RoleMenuResp getRoleMenu(Long relationId) {
        log.info("查询角色菜单授权详情，relationId={}", relationId);
        SysRoleMenuEntity relation = requireRoleMenu(relationId);
        SysRoleEntity role = requireRole(relation.getRoleId());
        SysMenuEntity menu = requireMenu(relation.getMenuId());
        RoleMenuResp resp = new RoleMenuResp();
        resp.setKey(String.valueOf(relation.getId()));
        resp.setRoleName(role.getRoleName());
        resp.setMenuName(menu.getMenuName());
        resp.setPerms(menu.getPerms());
        return resp;
    }

    /**
     * 绑定角色菜单。
     *
     * @param request 角色菜单绑定请求
     * @return 绑定结果
     */
    @Transactional(rollbackFor = Exception.class)
    public RelationBindingResp bindRoleMenu(BindRoleMenuReq request) {
        log.info("绑定角色菜单，roleId={}，menuId={}", request.getRoleId(), request.getMenuId());
        requireRole(request.getRoleId());
        requireMenu(request.getMenuId());
        SysRoleMenuEntity existed = sysRoleMenuMapper.selectOne(new LambdaQueryWrapper<SysRoleMenuEntity>()
            .eq(SysRoleMenuEntity::getRoleId, request.getRoleId())
            .eq(SysRoleMenuEntity::getMenuId, request.getMenuId())
            .last("limit 1"));
        if (existed != null) {
            log.info("角色菜单关系已存在，roleId={}，menuId={}", request.getRoleId(), request.getMenuId());
            return authorizationConverter.toRelationBindingVO(existed, request.getRoleId(), request.getMenuId());
        }
        SysRoleMenuEntity entity = new SysRoleMenuEntity();
        entity.setRoleId(request.getRoleId());
        entity.setMenuId(request.getMenuId());
        sysRoleMenuMapper.insert(entity);
        return authorizationConverter.toRelationBindingVO(entity, request.getRoleId(), request.getMenuId());
    }

    /**
     * 删除角色菜单授权。
     *
     * @param relationId 关系编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleMenu(Long relationId) {
        log.info("删除角色菜单授权，relationId={}", relationId);
        requireRoleMenu(relationId);
        sysRoleMenuMapper.deleteById(relationId);
    }

    /**
     * 校验用户存在。
     *
     * @param userId 用户业务编号
     * @return 用户实体
     */
    private SysUserEntity requireUser(String userId) {
        return MybatisTenantHelpers.requireEntity(sysUserMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysUserEntity::getDeleted)
                .eq(SysUserEntity::getUserId, userId)
                .last("limit 1")), "用户不存在");
    }

    /**
     * 校验角色存在。
     *
     * @param roleId 角色编号
     * @return 角色实体
     */
    private SysRoleEntity requireRole(Long roleId) {
        return MybatisTenantHelpers.requireEntity(sysRoleMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysRoleEntity::getDeleted)
                .eq(SysRoleEntity::getId, roleId)
                .last("limit 1")), "角色不存在");
    }

    /**
     * 校验菜单存在。
     *
     * @param menuId 菜单编号
     * @return 菜单实体
     */
    private SysMenuEntity requireMenu(Long menuId) {
        return MybatisTenantHelpers.requireEntity(sysMenuMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysMenuEntity::getDeleted)
                .eq(SysMenuEntity::getId, menuId)
                .last("limit 1")), "菜单不存在");
    }

    /**
     * 校验用户角色关系存在。
     *
     * @param relationId 关系编号
     * @return 用户角色关系实体
     */
    private SysUserRoleEntity requireUserRole(Long relationId) {
        return MybatisTenantHelpers.requireEntity(sysUserRoleMapper.selectById(relationId), "用户角色关系不存在");
    }

    /**
     * 校验角色菜单关系存在。
     *
     * @param relationId 关系编号
     * @return 角色菜单关系实体
     */
    private SysRoleMenuEntity requireRoleMenu(Long relationId) {
        return MybatisTenantHelpers.requireEntity(sysRoleMenuMapper.selectById(relationId), "角色菜单关系不存在");
    }
}
