package com.hlw.auth.client;

import com.hlw.common.core.domain.R;
import com.hlw.common.core.domain.system.req.InternalLoginInfoReq;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * hospital-system 内部日志 Feign 客户端。
 */
@FeignClient(name = "hospital-system", contextId = "systemLogFeignClient")
public interface SystemLogFeignClient {
    /**
     * 写入系统登录日志。
     *
     * @param request 登录日志写入请求
     * @return 空响应
     */
    @PostMapping("/internal/log/login")
    R<Void> recordLoginInfo(@RequestBody InternalLoginInfoReq request);
}
