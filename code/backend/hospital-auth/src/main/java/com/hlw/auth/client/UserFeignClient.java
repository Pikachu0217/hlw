package com.hlw.auth.client;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.domain.system.resp.InternalUserResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * hospital-system 内部用户查询 Feign 客户端。
 * <p>通过 Nacos 服务发现直连 hospital-system 服务，绕过网关，
 * 因此 internal/** 路径不应被网关对外暴露。</p>
 */
@FeignClient(name = "hospital-system", contextId = "systemUserFeignClient")
public interface UserFeignClient {
    /**
     * 按租户编号和登录账号查询用户。
     *
     * @param tenantId 租户编号
     * @param username 登录账号
     * @return 用户数据，不存在时 data 为 null
     */
    @GetMapping("/internal/users")
    R<InternalUserResp> users(@RequestParam("tenantId") Long tenantId, @RequestParam("username") String username);

    /**
     * 按用户编号和租户编号查询用户。
     *
     * @param id 用户编号
     * @param tenantId 租户编号
     * @return 用户数据，不存在时 data 为 null
     */
    @GetMapping("/internal/users/{id}")
    R<InternalUserResp> detail(@PathVariable("id") Long id, @RequestParam("tenantId") Long tenantId);
}
