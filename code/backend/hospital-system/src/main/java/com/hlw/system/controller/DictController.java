package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.dto.CreateDictRequest;
import com.hlw.system.service.DictService;
import com.hlw.system.vo.DictVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 字典管理控制器。
 */
@RestController
@RequestMapping("/system/dict")
@RequiredArgsConstructor
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
    public R<PageResult<DictVO>> list(PageQuery query) {
        return R.ok(dictService.listDicts(query));
    }

    /**
     * 创建字典项。
     *
     * @param request 字典创建命令
     * @return 创建后的字典项
     */
    @PostMapping
    public R<DictVO> createDict(@Valid @RequestBody CreateDictRequest request) {
        return R.ok(dictService.createDict(request));
    }
}
