package com.hlw.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hlw.auth.entity.AuthUserEntity;
import com.hlw.auth.mapper.AuthUserMapper;
import com.hlw.auth.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * MyBatis Plus 用户仓储，用于从认证用户表读取登录账号和用户资料。
 */
@Repository
@RequiredArgsConstructor
public class MybatisUserRepository implements UserRepository {
    /** 认证用户数据访问组件。 */
    private final AuthUserMapper authUserMapper;

    /**
     * 按租户编号和登录账号查询用户。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @return 登录用户，不存在返回 null
     */
    @Override
    public LoginUser findByTenantIdAndUsername(Long tenantId, String username) {
        AuthUserEntity entity = authUserMapper.selectOne(new LambdaQueryWrapper<AuthUserEntity>()
            .eq(AuthUserEntity::getDeleted, 0)
            .eq(AuthUserEntity::getTenantId, tenantId)
            .eq(AuthUserEntity::getUsername, username)
            .in(AuthUserEntity::getStatus, "1", "启用", "ACTIVE")
            .orderByAsc(AuthUserEntity::getId)
            .last("limit 1"));
        if (entity == null) {
            return null;
        }
        return new LoginUser(
            entity.getId(),
            entity.getTenantId(),
            entity.getUsername(),
            entity.getPassword(),
            entity.getUserType()
        );
    }

    /**
     * 按用户编号和租户编号查询用户资料。
     *
     * @param id 用户编号
     * @param tenantId 租户编号
     * @return 用户资料，不存在返回 null
     */
    @Override
    public UserProfileVO findProfileById(Long id, Long tenantId) {
        AuthUserEntity entity = authUserMapper.selectOne(new LambdaQueryWrapper<AuthUserEntity>()
            .eq(AuthUserEntity::getDeleted, 0)
            .eq(AuthUserEntity::getId, id)
            .eq(AuthUserEntity::getTenantId, tenantId)
            .last("limit 1"));
        if (entity == null) {
            return null;
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setUserId(entity.getId());
        vo.setTenantId(entity.getTenantId());
        vo.setUsername(entity.getUsername());
        vo.setPhone(entity.getPhone());
        vo.setUserType(entity.getUserType());
        vo.setRoleName(entity.getUserType());
        vo.setStatus(entity.getStatus());
        return vo;
    }
}
