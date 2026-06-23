package com.hlw.doctor.client;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.domain.system.resp.InternalUserResp;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * hospital-system 内部用户 Feign 客户端。
 */
@FeignClient(name = "hospital-system", contextId = "doctorSystemUserFeignClient")
public interface SystemUserFeignClient {
    /**
     * 按用户类型查询租户用户列表。
     *
     * @param tenantId 租户编号
     * @param userType 用户类型
     * @return 用户列表
     */
    @GetMapping("/internal/users/by-type")
    R<List<InternalUserResp>> listByUserType(@RequestParam("tenantId") Long tenantId, @RequestParam("userType") String userType);
}
