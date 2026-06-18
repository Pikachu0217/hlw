package com.hlw.auth.controller;

import com.hlw.auth.domain.req.CreateLoginRecordReq;
import com.hlw.auth.domain.req.UpdateLoginRecordReq;
import com.hlw.auth.domain.resp.LoginRecordResp;
import com.hlw.auth.service.LoginRecordService;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录记录管理控制器。
 */
@RestController
@RequestMapping("/auth/login-record")
@RequiredArgsConstructor
@Slf4j
public class LoginRecordController {
    /** 登录记录聚合服务。 */
    private final LoginRecordService loginRecordService;

    /**
     * 分页查询登录记录列表。
     *
     * @param query 分页查询参数
     * @return 登录记录分页结果
     */
    @GetMapping
    public R<PageResult<LoginRecordResp>> list(PageQuery query) {
        log.info("查询登录记录列表，keyword={}", query.getKeyword());
        return R.ok(loginRecordService.listLoginRecords(query));
    }

    /**
     * 创建登录记录。
     *
     * @param request 登录记录创建命令
     * @return 创建后的登录记录
     */
    @PostMapping
    public R<LoginRecordResp> createLoginRecord(@Valid @RequestBody CreateLoginRecordReq request) {
        log.info("创建登录记录，username={}，loginStatus={}", request.getUsername(), request.getLoginStatus());
        return R.ok(loginRecordService.createLoginRecord(request));
    }

    /**
     * 查询登录记录详情。
     *
     * @param id 登录记录编号
     * @return 登录记录详情
     */
    @GetMapping("/{id}")
    public R<LoginRecordResp> detail(@PathVariable Long id) {
        log.info("查询登录记录详情，id={}", id);
        return R.ok(loginRecordService.getLoginRecord(id));
    }

    /**
     * 更新登录记录。
     *
     * @param id 登录记录编号
     * @param request 登录记录更新命令
     * @return 更新后的登录记录
     */
    @PutMapping("/{id}")
    public R<LoginRecordResp> updateLoginRecord(@PathVariable Long id, @Valid @RequestBody UpdateLoginRecordReq request) {
        log.info("更新登录记录，id={}，loginStatus={}", id, request.getLoginStatus());
        return R.ok(loginRecordService.updateLoginRecord(id, request));
    }

    /**
     * 删除登录记录。
     *
     * @param id 登录记录编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteLoginRecord(@PathVariable Long id) {
        log.info("删除登录记录，id={}", id);
        loginRecordService.deleteLoginRecord(id);
        return R.ok(null);
    }
}
