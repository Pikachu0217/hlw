package com.hlw.auth.service;

import com.hlw.auth.domain.resp.LoginUserResp;
import com.hlw.auth.domain.resp.UserDetailResp;

/**
 * 用户仓储接口，封装认证模块需要的用户查询能力。
 */
public interface UserRepository {
    /**
     * 按租户编号和登录账号查询用户。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @return 登录用户，不存在返回 null
     */
    LoginUserResp findByTenantIdAndUsername(Long tenantId, String username);

    /**
     * 按用户编号和租户编号查询用户资料。
     *
     * @param id 用户编号
     * @param tenantId 租户编号
     * @return 用户资料，不存在返回 null
     */
    UserDetailResp findProfileById(Long id, Long tenantId);
}
