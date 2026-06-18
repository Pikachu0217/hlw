package com.hlw.system.controller;

import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.domain.R;
import com.hlw.system.domain.req.CreateDeptReq;
import com.hlw.system.service.DeptService;
import com.hlw.system.domain.resp.DeptResp;
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

import java.util.List;

/**
 * 部门管理控制器。
 */
@RestController
@RequestMapping("/system/dept")
@RequiredArgsConstructor
@Slf4j
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
    public R<PageResult<DeptResp>> list(PageQuery query) {
        log.info("查询部门列表，keyword={}", query.getKeyword());
        return R.ok(deptService.listDepts(query));
    }

    /**
     * 查询部门选项。
     *
     * @param query 查询参数
     * @return 部门选项列表
     */
    @GetMapping("/options")
    public R<List<DeptResp>> options(PageQuery query) {
        log.info("查询部门选项，keyword={}", query.getKeyword());
        return R.ok(deptService.listDeptOptions(query));
    }

    /**
     * 创建部门。
     *
     * @param request 部门创建命令
     * @return 创建后的部门
     */
    @PostMapping
    public R<DeptResp> createDept(@Valid @RequestBody CreateDeptReq request) {
        log.info("创建部门，deptName={}", request.getDeptName());
        return R.ok(deptService.createDept(request));
    }

    /**
     * 查询部门详情。
     *
     * @param id 部门编号
     * @return 部门详情
     */
    @GetMapping("/{id}")
    public R<DeptResp> detail(@PathVariable Long id) {
        log.info("查询部门详情，id={}", id);
        return R.ok(deptService.getDept(id));
    }

    /**
     * 更新部门。
     *
     * @param id 部门编号
     * @param request 部门更新命令
     * @return 更新后的部门
     */
    @PutMapping("/{id}")
    public R<DeptResp> updateDept(@PathVariable Long id, @Valid @RequestBody CreateDeptReq request) {
        log.info("更新部门，id={}，deptName={}", id, request.getDeptName());
        return R.ok(deptService.updateDept(id, request));
    }

    /**
     * 删除部门。
     *
     * @param id 部门编号
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public R<Void> deleteDept(@PathVariable Long id) {
        log.info("删除部门，id={}", id);
        deptService.deleteDept(id);
        return R.ok(null);
    }
}
