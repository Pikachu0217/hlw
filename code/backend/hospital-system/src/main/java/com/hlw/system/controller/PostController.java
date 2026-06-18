package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.domain.req.CreatePostReq;
import com.hlw.system.service.PostService;
import com.hlw.system.domain.resp.PostResp;
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
 * 岗位管理控制器。
 */
@RestController
@RequestMapping("/system/post")
@RequiredArgsConstructor
@Slf4j
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
    public R<PageResult<PostResp>> list(PageQuery query) {
        log.info("查询岗位列表，keyword={}", query.getKeyword());
        return R.ok(postService.listPosts(query));
    }

    /**
     * 创建岗位。
     *
     * @param request 岗位创建命令
     * @return 创建后的岗位
     */
    @PostMapping
    public R<PostResp> createPost(@Valid @RequestBody CreatePostReq request) {
        log.info("创建岗位，postName={}", request.getPostName());
        return R.ok(postService.createPost(request));
    }

    /**
     * 查询岗位详情。
     *
     * @param id 岗位编号
     * @return 岗位详情
     */
    @GetMapping("/{id}")
    public R<PostResp> detail(@PathVariable Long id) {
        log.info("查询岗位详情，id={}", id);
        return R.ok(postService.getPost(id));
    }

    /**
     * 更新岗位。
     *
     * @param id 岗位编号
     * @param request 岗位更新命令
     * @return 更新后的岗位
     */
    @PutMapping("/{id}")
    public R<PostResp> updatePost(@PathVariable Long id, @Valid @RequestBody CreatePostReq request) {
        log.info("更新岗位，id={}，postName={}", id, request.getPostName());
        return R.ok(postService.updatePost(id, request));
    }

    /**
     * 删除岗位。
     *
     * @param id 岗位编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deletePost(@PathVariable Long id) {
        log.info("删除岗位，id={}", id);
        postService.deletePost(id);
        return R.ok(null);
    }
}
