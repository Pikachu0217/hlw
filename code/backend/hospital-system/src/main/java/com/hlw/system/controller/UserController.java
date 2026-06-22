package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.domain.req.CreateUserReq;
import com.hlw.system.service.UserService;
import com.hlw.system.domain.resp.UserResp;
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
 * 后台用户管理控制器。
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    /** 后台用户聚合服务。 */
    private final UserService userService;

    /**
     * 分页查询后台用户列表。
     *
     * @param query 分页查询参数
     * @return 后台用户分页结果
     */
    @GetMapping
    public R<PageResult<UserResp>> list(PageQuery query) {
        log.info("查询后台用户列表，keyword={}", query.getKeyword());
        return R.ok(userService.listUsers(query));
    }

    /**
     * 创建后台用户。
     *
     * @param request 用户创建命令
     * @return 创建后的用户
     */
    @PostMapping
    public R<UserResp> createUser(@Valid @RequestBody CreateUserReq request) {
        log.info("创建后台用户，userName={}，realName={}", request.getUserName(), request.getRealName());
        return R.ok(userService.createUser(request));
    }

    /**
     * 查询后台用户详情。
     *
     * @param id 用户编号
     * @return 后台用户详情
     */
    @GetMapping("/{id}")
    public R<UserResp> detail(@PathVariable Long id) {
        log.info("查询后台用户详情，id={}", id);
        return R.ok(userService.getUser(id));
    }

    /**
     * 更新后台用户。
     *
     * @param id 用户编号
     * @param request 用户更新命令
     * @return 更新后的用户
     */
    @PutMapping("/{id}")
    public R<UserResp> updateUser(@PathVariable Long id, @Valid @RequestBody CreateUserReq request) {
        log.info("更新后台用户，id={}，userName={}，realName={}", id, request.getUserName(), request.getRealName());
        return R.ok(userService.updateUser(id, request));
    }

    /**
     * 删除后台用户。
     *
     * @param id 用户编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteUser(@PathVariable Long id) {
        log.info("删除后台用户，id={}", id);
        userService.deleteUser(id);
        return R.ok(null);
    }
}
