package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.domain.req.CreateDictReq;
import com.hlw.system.service.DictService;
import com.hlw.system.domain.resp.DictResp;
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
 * 字典管理控制器。
 */
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
@Slf4j
public class DictController {
    /** 字典聚合服务。 */
    private final DictService dictService;

    /**
     * 分页查询字典列表。
     *
     * @param query 分页查询参数
     * @return 字典分页结果
     */
    @GetMapping
    public R<PageResult<DictResp>> list(PageQuery query) {
        log.info("查询字典列表，keyword={}", query.getKeyword());
        return R.ok(dictService.listDicts(query));
    }

    /**
     * 创建字典项。
     *
     * @param request 字典创建命令
     * @return 创建后的字典项
     */
    @PostMapping
    public R<DictResp> createDict(@Valid @RequestBody CreateDictReq request) {
        log.info("创建字典项，dictType={}，dictLabel={}", request.getDictType(), request.getDictLabel());
        return R.ok(dictService.createDict(request));
    }

    /**
     * 查询字典详情。
     *
     * @param id 字典编号
     * @return 字典详情
     */
    @GetMapping("/{id}")
    public R<DictResp> detail(@PathVariable Long id) {
        log.info("查询字典详情，id={}", id);
        return R.ok(dictService.getDict(id));
    }

    /**
     * 更新字典项。
     *
     * @param id 字典编号
     * @param request 字典更新命令
     * @return 更新后的字典项
     */
    @PutMapping("/{id}")
    public R<DictResp> updateDict(@PathVariable Long id, @Valid @RequestBody CreateDictReq request) {
        log.info("更新字典项，id={}，dictType={}，dictLabel={}", id, request.getDictType(), request.getDictLabel());
        return R.ok(dictService.updateDict(id, request));
    }

    /**
     * 删除字典项。
     *
     * @param id 字典编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteDict(@PathVariable Long id) {
        log.info("删除字典项，id={}", id);
        dictService.deleteDict(id);
        return R.ok(null);
    }
}
