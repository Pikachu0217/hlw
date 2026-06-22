package com.hlw.auth.service;

import com.hlw.auth.client.UserFeignClient;
import com.hlw.auth.domain.resp.LoginUserResp;
import com.hlw.auth.domain.resp.UserDetailResp;
import com.hlw.common.core.domain.R;
import com.hlw.common.core.domain.system.resp.InternalUserResp;
import com.hlw.common.core.enums.HttpStatusEnum;
import com.hlw.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * 基于 OpenFeign 的用户仓储，通过调用 hospital-system 内部接口完成用户查询。
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class FeignUserRepository implements UserRepository {
    /** 系统用户 Feign 客户端。 */
    private final UserFeignClient userFeignClient;

    /**
     * 按租户编号和登录账号查询用户。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @return 登录用户，不存在返回 null
     */
    @Override
    public LoginUserResp findByTenantIdAndUsername(Long tenantId, String username) {
        log.info("Feign 调用 hospital-system 查询用户，tenantId={}，username={}", tenantId, username);
        R<InternalUserResp> response = userFeignClient.users(tenantId, username);
        InternalUserResp data = requireOk(response, "查询用户失败");
        if (data == null) {
            return null;
        }
        return new LoginUserResp(
            data.getId(),
            data.getUserId(),
            data.getTenantId(),
            data.getUsername(),
            data.getPassword(),
            data.getUserType()
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
    public UserDetailResp findProfileById(Long id, Long tenantId) {
        log.info("Feign 调用 hospital-system 查询用户资料，id={}，tenantId={}", id, tenantId);
        R<InternalUserResp> response = userFeignClient.detail(id, tenantId);
        InternalUserResp data = requireOk(response, "查询用户资料失败");
        if (data == null) {
            return null;
        }
        UserDetailResp resp = new UserDetailResp();
        resp.setId(data.getId());
        resp.setUserId(data.getId());
        resp.setBusinessUserId(data.getUserId());
        resp.setTenantId(data.getTenantId());
        resp.setUsername(data.getUsername());
        resp.setPhone(data.getPhone());
        resp.setUserType(data.getUserType());
        resp.setRoleCode(data.getRoleCode());
        resp.setResourceRoutePathList(data.getResourceRoutePathList());
        resp.setStatus(data.getStatus());
        return resp;
    }

    /**
     * 校验远程响应是否正常。
     *
     * @param response Feign 响应
     * @param message 错误消息
     * @return 响应数据
     */
    private InternalUserResp requireOk(R<InternalUserResp> response, String message) {
        if (response == null) {
            log.warn("Feign 调用返回空响应");
            throw new BizException(HttpStatusEnum.REMOTE_SYSTEM_RESPONSE_NULL, message);
        }
        if (response.code() != 200) {
            log.warn("Feign 调用返回错误，code={}，message={}", response.code(), response.message());
            throw new BizException(HttpStatusEnum.REMOTE_SYSTEM_RESPONSE_ERROR, response.message() == null ? message : response.message());
        }
        return response.data();
    }
}
