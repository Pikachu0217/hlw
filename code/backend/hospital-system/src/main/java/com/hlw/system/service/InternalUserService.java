package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.hlw.common.core.domain.system.resp.InternalUserResp;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.system.entity.SysRoleEntity;
import com.hlw.system.entity.SysUserEntity;
import com.hlw.system.entity.SysUserRoleEntity;
import com.hlw.system.mapper.SysRoleMapper;
import com.hlw.system.mapper.SysUserMapper;
import com.hlw.system.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * 内部用户查询服务，承接 hospital-auth 通过 OpenFeign 发起的登录认证与资料查询。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InternalUserService {

    /**
     * 用户数据访问组件。
     */
    private final SysUserMapper sysUserMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 按租户编号和登录账号查询用户。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @return 内部用户展示对象，不存在返回 null
     */
    public InternalUserResp findByTenantIdAndUsername(Long tenantId, String username) {
        log.info("内部查询用户，tenantId={}，username={}", tenantId, username);
        LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getDeleted, DeletedStatusEnum.NOT_DELETED.getType())
                .eq(SysUserEntity::getTenantId, tenantId)
                .eq(SysUserEntity::getUsername, username)
                .eq(SysUserEntity::getStatus, CommonStatusEnum.ENABLED.getStatus())
                .orderByAsc(SysUserEntity::getId)
                .last("limit 1");
        SysUserEntity entity = InterceptorIgnoreHelper.execute(
                ignoreTenantLine(),
                () -> sysUserMapper.selectOne(queryWrapper)
        );
        if (null == entity) {
            return null;
        }
        return toInternalUserResp(entity, queryRoleCodeByUserIdAndTenantId(entity.getId(), tenantId));
    }

    /**
     * 按用户编号和租户编号查询用户。
     *
     * @param id       用户编号
     * @param tenantId 租户编号
     * @return 内部用户展示对象，不存在返回 null
     */
    public InternalUserResp findByIdAndTenantId(Long uid, Long tenantId) {
        LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getDeleted, DeletedStatusEnum.NOT_DELETED.getType())
                .eq(SysUserEntity::getId, uid)
                .eq(SysUserEntity::getTenantId, tenantId)
                .last("limit 1");
        SysUserEntity entity = sysUserMapper.selectOne(queryWrapper);
        String roleCode = queryRoleCodeByUserIdAndTenantId(uid, tenantId);
        return entity == null ? null : toInternalUserResp(entity, roleCode);
    }

    /**
     * 根据用户 id 和租户 id 查询 roleCode
     * @param uid
     * @param tenantId
     * @return
     */
    private String queryRoleCodeByUserIdAndTenantId(Long uid, Long tenantId) {
        LambdaQueryWrapper<SysUserRoleEntity> userRoleQueryWrapper = new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getDeleted, DeletedStatusEnum.NOT_DELETED.getType())
                .eq(SysUserRoleEntity::getId, uid)
                .eq(SysUserRoleEntity::getTenantId, tenantId)
                .last("limit 1");
        SysUserRoleEntity sysUserRoleEntity = sysUserRoleMapper.selectOne(userRoleQueryWrapper);
        if (null == sysUserRoleEntity) {
            return null;
        }
        LambdaQueryWrapper<SysRoleEntity> roleQueryWrapper = new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getDeleted, DeletedStatusEnum.NOT_DELETED.getType())
                .eq(SysRoleEntity::getId, uid)
                .eq(SysRoleEntity::getTenantId, tenantId)
                .last("limit 1");
        SysRoleEntity roleEntity = sysRoleMapper.selectOne(roleQueryWrapper);
        if (null == roleEntity) {
            return null;
        }
        return roleEntity.getRoleCode();
    }

    /**
     * 构造忽略租户行拦截策略。
     *
     * @return 忽略策略
     */
    private IgnoreStrategy ignoreTenantLine() {
        return IgnoreStrategy.builder().tenantLine(true).build();
    }

    /**
     * 转换为内部用户展示对象。
     *
     * @param entity 用户实体
     * @return 内部用户展示对象
     */
    private InternalUserResp toInternalUserResp(SysUserEntity entity, String roleCode) {
        InternalUserResp resp = new InternalUserResp();
        resp.setId(entity.getId());
        resp.setTenantId(entity.getTenantId());
        resp.setUsername(entity.getUsername());
        resp.setPassword(entity.getPassword());
        resp.setPhone(entity.getPhone());
        resp.setUserType(entity.getUserType());
        resp.setRoleCode(roleCode);
        resp.setStatus(entity.getStatus());
        return resp;
    }
}
