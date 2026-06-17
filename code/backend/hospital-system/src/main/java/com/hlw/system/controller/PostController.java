package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.dto.CreatePostRequest;
import com.hlw.system.service.PostService;
import com.hlw.system.vo.PostVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 岗位管理控制器。
 */
@RestController
@RequestMapping("/system/post")
@RequiredArgsConstructor
public class PostController {
    /** 岗位聚合服务。 */
    private final PostService postService;

    /**
     * 分页查询岗位列表。
     *
     * @param query 分页查询参数
     * @return 岗位分页结果
     */
    @GetMapping
    public R<PageResult<PostVO>> list(PageQuery query) {
        return R.ok(postService.listPosts(query));
    }

    /**
     * 创建岗位。
     *
     * @param request 岗位创建命令
     * @return 创建后的岗位
     */
    @PostMapping
    public R<PostVO> createPost(@Valid @RequestBody CreatePostRequest request) {
        return R.ok(postService.createPost(request));
    }
}
