package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.domain.req.CreateNoticeReq;
import com.hlw.system.domain.resp.NoticeResp;
import com.hlw.system.service.NoticeService;
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
 * 通知公告管理控制器。
 */
@RestController
@RequestMapping("/system/notice")
@RequiredArgsConstructor
@Slf4j
public class NoticeController {
    /** 通知公告聚合服务。 */
    private final NoticeService noticeService;

    /**
     * 分页查询通知公告列表。
     *
     * @param query 分页查询参数
     * @return 通知公告分页结果
     */
    @GetMapping
    public R<PageResult<NoticeResp>> list(PageQuery query) {
        log.info("查询通知公告列表，keyword={}", query.getKeyword());
        return R.ok(noticeService.listNotices(query));
    }

    /**
     * 创建通知公告。
     *
     * @param request 通知公告创建命令
     * @return 通知公告展示对象
     */
    @PostMapping
    public R<NoticeResp> createNotice(@Valid @RequestBody CreateNoticeReq request) {
        log.info("创建通知公告，noticeTitle={}", request.getNoticeTitle());
        return R.ok(noticeService.createNotice(request));
    }

    /**
     * 查询通知公告详情。
     *
     * @param id 通知公告编号
     * @return 通知公告展示对象
     */
    @GetMapping("/{id}")
    public R<NoticeResp> detail(@PathVariable Long id) {
        log.info("查询通知公告详情，id={}", id);
        return R.ok(noticeService.getNotice(id));
    }

    /**
     * 更新通知公告。
     *
     * @param id 通知公告编号
     * @param request 通知公告更新命令
     * @return 通知公告展示对象
     */
    @PutMapping("/{id}")
    public R<NoticeResp> updateNotice(@PathVariable Long id, @Valid @RequestBody CreateNoticeReq request) {
        log.info("更新通知公告，id={}，noticeTitle={}", id, request.getNoticeTitle());
        return R.ok(noticeService.updateNotice(id, request));
    }

    /**
     * 删除通知公告。
     *
     * @param id 通知公告编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteNotice(@PathVariable Long id) {
        log.info("删除通知公告，id={}", id);
        noticeService.deleteNotice(id);
        return R.ok(null);
    }
}
