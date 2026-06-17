package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.dto.CreateUserRequest;
import com.hlw.system.service.UserService;
import com.hlw.system.vo.UserVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台用户管理控制器。
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
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
    public R<PageResult<UserVO>> list(PageQuery query) {
        return R.ok(userService.listUsers(query));
    }

    /**
     * 创建后台用户。
     *
     * @param request 用户创建命令
     * @return 创建后的用户
     */
    @PostMapping
    public R<UserVO> createUser(@Valid @RequestBody CreateUserRequest request) {
        return R.ok(userService.createUser(request));
    }
}
