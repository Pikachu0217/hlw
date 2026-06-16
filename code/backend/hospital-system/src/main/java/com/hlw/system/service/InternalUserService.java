package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.system.entity.SysUserEntity;
import com.hlw.system.mapper.SysUserMapper;
import com.hlw.system.vo.InternalUserVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 内部用户查询服务，承接 hospital-auth 通过 OpenFeign 发起的登录认证与资料查询。
 */
@Service
@RequiredArgsConstructor
public class InternalUserService {
    private static final Logger log = LoggerFactory.getLogger(InternalUserService.class);

    /** 用户数据访问组件。 */
    private final SysUserMapper sysUserMapper;

    /**
     * 按租户编号和登录账号查询用户。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @return 内部用户展示对象，不存在返回 null
     */
    public InternalUserVO findByTenantIdAndUsername(Long tenantId, String username) {
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
        return entity == null ? null : toInternalUserVO(entity);
    }

    /**
     * 按用户编号和租户编号查询用户。
     *
     * @param id 用户编号
     * @param tenantId 租户编号
     * @return 内部用户展示对象，不存在返回 null
     */
    public InternalUserVO findByIdAndTenantId(Long id, Long tenantId) {
        log.info("内部查询用户资料，id={}，tenantId={}", id, tenantId);
        LambdaQueryWrapper<SysUserEntity> queryWrapper = new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getDeleted, DeletedStatusEnum.NOT_DELETED.getType())
            .eq(SysUserEntity::getId, id)
            .eq(SysUserEntity::getTenantId, tenantId)
            .last("limit 1");
        SysUserEntity entity = InterceptorIgnoreHelper.execute(
            ignoreTenantLine(),
            () -> sysUserMapper.selectOne(queryWrapper)
        );
        return entity == null ? null : toInternalUserVO(entity);
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
    private InternalUserVO toInternalUserVO(SysUserEntity entity) {
        InternalUserVO vo = new InternalUserVO();
        vo.setId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setUsername(entity.getUsername());
        vo.setPassword(entity.getPassword());
        vo.setPhone(entity.getPhone());
        vo.setUserType(entity.getUserType());
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
