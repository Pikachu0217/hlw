package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.dto.CreateDeptRequest;
import com.hlw.system.service.DeptService;
import com.hlw.system.vo.DeptVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 部门管理控制器。
 */
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
public class DeptController {
    /** 部门聚合服务。 */
    private final DeptService deptService;

    /**
     * 分页查询部门列表。
     *
     * @param query 分页查询参数
     * @return 部门分页结果
     */
    @GetMapping
    public R<PageResult<DeptVO>> list(PageQuery query) {
        return R.ok(deptService.listDepts(query));
    }

    /**
     * 查询部门选项。
     *
     * @param query 查询参数
     * @return 部门选项列表
     */
    @GetMapping("/options")
    public R<List<DeptVO>> options(PageQuery query) {
        return R.ok(deptService.listDeptOptions(query));
    }

    /**
     * 创建部门。
     *
     * @param request 部门创建命令
     * @return 创建后的部门
     */
    @PostMapping
    public R<DeptVO> createDept(@Valid @RequestBody CreateDeptRequest request) {
        return R.ok(deptService.createDept(request));
    }
}
