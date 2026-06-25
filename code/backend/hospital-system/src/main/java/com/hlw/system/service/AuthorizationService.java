package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
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
import java.util.List;
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
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        log.info("查询用户角色授权列表，tenantId={}", tenantId);
        Map<String, SysUserEntity> userMap = sysUserMapper.selectList(new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getTenantId, tenantId))
            .stream()
            .collect(Collectors.toMap(SysUserEntity::getUserId, user -> user, (left, right) -> left));
        Map<Long, String> roleMap = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getTenantId, tenantId))
            .stream()
            .collect(Collectors.toMap(SysRoleEntity::getId, SysRoleEntity::getRoleName, (left, right) -> left));
        return sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getTenantId, tenantId))
            .stream()
            .sorted(Comparator.comparing(SysUserRoleEntity::getId))
            .map(relation -> {
                UserRoleResp resp = new UserRoleResp();
                SysUserEntity user = userMap.get(relation.getUserId());
                resp.setId(relation.getId());
                resp.setUserId(relation.getUserId());
                resp.setUserName(user == null ? "-" : user.getUserName());
                resp.setRoleId(relation.getRoleId());
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
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        log.info("查询用户角色授权详情，tenantId={}，relationId={}", tenantId, relationId);
        SysUserRoleEntity relation = requireUserRole(tenantId, relationId);
        SysUserEntity user = requireUser(tenantId, relation.getUserId());
        SysRoleEntity role = requireRole(tenantId, relation.getRoleId());
        UserRoleResp resp = new UserRoleResp();
        resp.setId(relation.getId());
        resp.setUserId(relation.getUserId());
        resp.setUserName(user.getUserName());
        resp.setRoleId(relation.getRoleId());
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
    public List<RelationBindingResp> bindUserRole(BindUserRoleReq request) {
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        log.info("绑定用户角色，tenantId={}，userId={}，roleIds={}", tenantId, request.getUserId(), request.getRoleIds());
        SysUserEntity user = requireUser(tenantId, request.getUserId());
        request.getRoleIds().forEach(roleId -> requireRole(tenantId, roleId));
        sysUserRoleMapper.update(null, new LambdaUpdateWrapper<SysUserRoleEntity>()
            .eq(SysUserRoleEntity::getTenantId, tenantId)
            .eq(SysUserRoleEntity::getUserId, request.getUserId())
            .set(SysUserRoleEntity::getDeleted, 1));
        return request.getRoleIds().stream()
            .distinct()
            .map(roleId -> {
                SysUserRoleEntity entity = new SysUserRoleEntity();
                entity.setTenantId(user.getTenantId());
                entity.setUserId(request.getUserId());
                entity.setRoleId(roleId);
                sysUserRoleMapper.insert(entity);
                return authorizationConverter.toRelationBindingVO(entity, request.getUserId(), roleId);
            })
            .toList();
    }

    /**
     * 删除用户角色授权。
     *
     * @param relationId 关系编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUserRole(Long relationId) {
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        log.info("删除用户角色授权，tenantId={}，relationId={}", tenantId, relationId);
        requireUserRole(tenantId, relationId);
        sysUserRoleMapper.update(null, new LambdaUpdateWrapper<SysUserRoleEntity>()
            .eq(SysUserRoleEntity::getTenantId, tenantId)
            .eq(SysUserRoleEntity::getId, relationId)
            .set(SysUserRoleEntity::getDeleted, 1));
    }

    /**
     * 查询角色菜单授权列表。
     *
     * @return 角色菜单授权列表
     */
    @Transactional(readOnly = true)
    public java.util.List<RoleMenuResp> listRoleMenus() {
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        log.info("查询角色菜单授权列表，tenantId={}", tenantId);
        Map<Long, String> roleMap = sysRoleMapper.selectList(new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getTenantId, tenantId))
            .stream()
            .collect(Collectors.toMap(SysRoleEntity::getId, SysRoleEntity::getRoleName, (left, right) -> left));
        Map<Long, SysMenuEntity> menuMap = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
                .eq(SysMenuEntity::getTenantId, tenantId))
            .stream()
            .collect(Collectors.toMap(SysMenuEntity::getId, menu -> menu, (left, right) -> left));
        return sysRoleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenuEntity>()
                .eq(SysRoleMenuEntity::getTenantId, tenantId))
            .stream()
            .sorted(Comparator.comparing(SysRoleMenuEntity::getId))
            .map(relation -> {
                RoleMenuResp resp = new RoleMenuResp();
                SysMenuEntity menu = menuMap.get(relation.getMenuId());
                resp.setId(relation.getId());
                resp.setRoleId(relation.getRoleId());
                resp.setRoleName(roleMap.getOrDefault(relation.getRoleId(), "-"));
                resp.setMenuId(relation.getMenuId());
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
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        log.info("查询角色菜单授权详情，tenantId={}，relationId={}", tenantId, relationId);
        SysRoleMenuEntity relation = requireRoleMenu(tenantId, relationId);
        SysRoleEntity role = requireRole(tenantId, relation.getRoleId());
        SysMenuEntity menu = requireMenu(tenantId, relation.getMenuId());
        RoleMenuResp resp = new RoleMenuResp();
        resp.setId(relation.getId());
        resp.setRoleId(relation.getRoleId());
        resp.setRoleName(role.getRoleName());
        resp.setMenuId(relation.getMenuId());
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
    public List<RelationBindingResp> bindRoleMenu(BindRoleMenuReq request) {
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        log.info("绑定角色菜单，tenantId={}，roleId={}，menuIds={}", tenantId, request.getRoleId(), request.getMenuIds());
        SysRoleEntity role = requireRole(tenantId, request.getRoleId());
        request.getMenuIds().forEach(menuId -> requireMenu(tenantId, menuId));
        sysRoleMenuMapper.update(null, new LambdaUpdateWrapper<SysRoleMenuEntity>()
            .eq(SysRoleMenuEntity::getTenantId, role.getTenantId())
            .eq(SysRoleMenuEntity::getRoleId, request.getRoleId())
            .set(SysRoleMenuEntity::getDeleted, 1));
        return request.getMenuIds().stream()
            .distinct()
            .map(menuId -> {
                SysRoleMenuEntity entity = new SysRoleMenuEntity();
                entity.setTenantId(role.getTenantId());
                entity.setRoleId(request.getRoleId());
                entity.setMenuId(menuId);
                sysRoleMenuMapper.insert(entity);
                return authorizationConverter.toRelationBindingVO(entity, request.getRoleId(), menuId);
            })
            .toList();
    }

    /**
     * 删除角色菜单授权。
     *
     * @param relationId 关系编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoleMenu(Long relationId) {
        String tenantId = MybatisTenantHelpers.currentTenantIdString();
        log.info("删除角色菜单授权，tenantId={}，relationId={}", tenantId, relationId);
        requireRoleMenu(tenantId, relationId);
        sysRoleMenuMapper.update(null, new LambdaUpdateWrapper<SysRoleMenuEntity>()
            .eq(SysRoleMenuEntity::getTenantId, tenantId)
            .eq(SysRoleMenuEntity::getId, relationId)
            .set(SysRoleMenuEntity::getDeleted, 1));
    }

    /**
     * 校验用户存在。
     *
     * @param tenantId 租户编号
     * @param userId 用户业务编号
     * @return 用户实体
     */
    private SysUserEntity requireUser(String tenantId, String userId) {
        return MybatisTenantHelpers.requireEntity(sysUserMapper.selectOne(
            new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getTenantId, tenantId)
                .eq(SysUserEntity::getUserId, userId)
                .last("limit 1")), "用户不存在");
    }

    /**
     * 校验角色存在。
     *
     * @param tenantId 租户编号
     * @param roleId 角色编号
     * @return 角色实体
     */
    private SysRoleEntity requireRole(String tenantId, Long roleId) {
        return MybatisTenantHelpers.requireEntity(sysRoleMapper.selectOne(
            new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getTenantId, tenantId)
                .eq(SysRoleEntity::getId, roleId)
                .last("limit 1")), "角色不存在");
    }

    /**
     * 校验菜单存在。
     *
     * @param tenantId 租户编号
     * @param menuId 菜单编号
     * @return 菜单实体
     */
    private SysMenuEntity requireMenu(String tenantId, Long menuId) {
        return MybatisTenantHelpers.requireEntity(sysMenuMapper.selectOne(
            new LambdaQueryWrapper<SysMenuEntity>()
                .eq(SysMenuEntity::getTenantId, tenantId)
                .eq(SysMenuEntity::getId, menuId)
                .last("limit 1")), "菜单不存在");
    }

    /**
     * 校验用户角色关系存在。
     *
     * @param tenantId 租户编号
     * @param relationId 关系编号
     * @return 用户角色关系实体
     */
    private SysUserRoleEntity requireUserRole(String tenantId, Long relationId) {
        return MybatisTenantHelpers.requireEntity(sysUserRoleMapper.selectOne(
            new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getTenantId, tenantId)
                .eq(SysUserRoleEntity::getId, relationId)
                .last("limit 1")), "用户角色关系不存在");
    }

    /**
     * 校验角色菜单关系存在。
     *
     * @param tenantId 租户编号
     * @param relationId 关系编号
     * @return 角色菜单关系实体
     */
    private SysRoleMenuEntity requireRoleMenu(String tenantId, Long relationId) {
        return MybatisTenantHelpers.requireEntity(sysRoleMenuMapper.selectOne(
            new LambdaQueryWrapper<SysRoleMenuEntity>()
                .eq(SysRoleMenuEntity::getTenantId, tenantId)
                .eq(SysRoleMenuEntity::getId, relationId)
                .last("limit 1")), "角色菜单关系不存在");
    }
}
