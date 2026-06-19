package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.domain.resp.LoginInfoResp;
import com.hlw.system.domain.resp.OperatorLogResp;
import com.hlw.system.service.LoginInfoService;
import com.hlw.system.service.OperatorLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统日志查询控制器。
 */
@RestController
@RequestMapping("/system/log")
@RequiredArgsConstructor
@Slf4j
public class SystemLogController {
    /** 登录日志聚合服务。 */
    private final LoginInfoService loginInfoService;
    /** 操作日志聚合服务。 */
    private final OperatorLogService operatorLogService;

    /**
     * 分页查询登录日志。
     *
     * @param query 分页查询参数
     * @return 登录日志分页结果
     */
    @GetMapping("/login")
    public R<PageResult<LoginInfoResp>> listLoginInfos(PageQuery query) {
        log.info("查询登录日志列表，keyword={}", query.getKeyword());
        return R.ok(loginInfoService.listLoginInfos(query));
    }

    /**
     * 分页查询操作日志。
     *
     * @param query 分页查询参数
     * @return 操作日志分页结果
     */
    @GetMapping("/operator")
    public R<PageResult<OperatorLogResp>> listOperatorLogs(PageQuery query) {
        log.info("查询操作日志列表，keyword={}", query.getKeyword());
        return R.ok(operatorLogService.listOperatorLogs(query));
    }
}
