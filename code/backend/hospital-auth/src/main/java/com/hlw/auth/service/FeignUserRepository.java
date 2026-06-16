package com.hlw.auth.service;

import com.hlw.auth.client.SystemUserDTO;
import com.hlw.auth.client.SystemUserFeignClient;
import com.hlw.auth.vo.UserProfileVO;
import com.hlw.common.core.domain.R;
import com.hlw.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

/**
 * 基于 OpenFeign 的用户仓储，通过调用 hospital-system 内部接口完成用户查询。
 */
@Repository
@RequiredArgsConstructor
public class FeignUserRepository implements UserRepository {
    private static final Logger log = LoggerFactory.getLogger(FeignUserRepository.class);

    /** 系统用户 Feign 客户端。 */
    private final SystemUserFeignClient systemUserFeignClient;

    /**
     * 按租户编号和登录账号查询用户。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @return 登录用户，不存在返回 null
     */
    @Override
    public LoginUser findByTenantIdAndUsername(Long tenantId, String username) {
        log.info("Feign 调用 hospital-system 查询用户，tenantId={}，username={}", tenantId, username);
        R<SystemUserDTO> response = systemUserFeignClient.lookup(tenantId, username);
        SystemUserDTO data = requireOk(response, "查询用户失败");
        if (data == null) {
            return null;
        }
        return new LoginUser(
            data.getId(),
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
    public UserProfileVO findProfileById(Long id, Long tenantId) {
        log.info("Feign 调用 hospital-system 查询用户资料，id={}，tenantId={}", id, tenantId);
        R<SystemUserDTO> response = systemUserFeignClient.detail(id, tenantId);
        SystemUserDTO data = requireOk(response, "查询用户资料失败");
        if (data == null) {
            return null;
        }
        UserProfileVO vo = new UserProfileVO();
        vo.setKey(String.valueOf(data.getId()));
        vo.setUserId(data.getId());
        vo.setTenantId(data.getTenantId());
        vo.setUsername(data.getUsername());
        vo.setPhone(data.getPhone());
        vo.setUserType(data.getUserType());
        // TODO: 待 role 子系统提供真实角色名查询
        vo.setRoleName(null);
        vo.setStatus(data.getStatus());
        return vo;
    }

    /**
     * 校验远程响应是否正常。
     *
     * @param response Feign 响应
     * @param message 错误消息
     * @return 响应数据
     */
    private SystemUserDTO requireOk(R<SystemUserDTO> response, String message) {
        if (response == null) {
            log.warn("Feign 调用返回空响应");
            throw new BizException(502, message);
        }
        if (response.code() != 200) {
            log.warn("Feign 调用返回错误，code={}，message={}", response.code(), response.message());
            throw new BizException(response.code(), response.message() == null ? message : response.message());
        }
        return response.data();
    }
}
