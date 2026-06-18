package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateDeptReq;
import com.hlw.system.entity.SysDeptEntity;
import com.hlw.system.mapper.SysDeptMapper;
import com.hlw.system.service.converter.DeptConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.domain.resp.DeptResp;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * 部门聚合服务，负责部门的查询与创建编排。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeptService {
    /** 部门数据访问组件。 */
    private final SysDeptMapper sysDeptMapper;
    /** 部门展示对象转换器。 */
    private final DeptConverter deptConverter;

    /**
     * 分页查询部门列表，按排序、主键升序排列。
     *
     * @param query 分页查询条件
     * @return 部门分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<DeptResp> listDepts(PageQuery query) {
        log.info("查询系统部门列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());

        Page<SysDeptEntity> page = query.toPage();
        LambdaQueryWrapper<SysDeptEntity> wrapper = buildListWrapper(query.getKeyword());
        Page<SysDeptEntity> result = sysDeptMapper.selectPage(page, wrapper);
        List<DeptResp> records = result.getRecords().stream()
            .map(deptConverter::toDeptVO)
            .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 查询部门选项列表。
     *
     * @param query 查询条件
     * @return 部门展示对象列表
     */
    @Transactional(readOnly = true)
    public List<DeptResp> listDeptOptions(PageQuery query) {
        log.info("查询系统部门选项，keyword={}", query.getKeyword());
        return sysDeptMapper.selectList(buildListWrapper(query.getKeyword())).stream()
            .map(deptConverter::toDeptVO)
            .toList();
    }

    /**
     * 创建部门。
     *
     * @param request 创建部门请求
     * @return 新建部门展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public DeptResp createDept(CreateDeptReq request) {
        Long parentId = DefaultValueUtils.defaultIfNull(request.getParentId(), 0L);
        log.info("创建部门，deptName={}，parentId={}", request.getDeptName(), parentId);
        SysDeptEntity entity = new SysDeptEntity();
        entity.setParentId(parentId);
        entity.setDeptName(request.getDeptName());
        entity.setAncestors(resolveAncestors(parentId));
        entity.setSort(DefaultValueUtils.defaultIfNull(request.getSort(), 0));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setDeleted(0);
        sysDeptMapper.insert(entity);
        return deptConverter.toDeptVO(entity);
    }

    /**
     * 查询部门详情。
     *
     * @param deptId 部门编号
     * @return 部门展示对象
     */
    @Transactional(readOnly = true)
    public DeptResp getDept(Long deptId) {
        log.info("查询系统部门详情，deptId={}", deptId);
        return deptConverter.toDeptVO(requireActiveDept(deptId));
    }

    /**
     * 更新部门。
     *
     * @param deptId 部门编号
     * @param request 部门更新请求
     * @return 更新后的部门展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public DeptResp updateDept(Long deptId, CreateDeptReq request) {
        Long parentId = DefaultValueUtils.defaultIfNull(request.getParentId(), 0L);
        log.info("更新系统部门，deptId={}，deptName={}，parentId={}", deptId, request.getDeptName(), parentId);
        SysDeptEntity entity = requireActiveDept(deptId);
        entity.setParentId(parentId);
        entity.setDeptName(request.getDeptName());
        entity.setAncestors(resolveAncestors(parentId));
        entity.setSort(DefaultValueUtils.defaultIfNull(request.getSort(), 0));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        sysDeptMapper.updateById(entity);
        return deptConverter.toDeptVO(entity);
    }

    /**
     * 删除部门。
     *
     * @param deptId 部门编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long deptId) {
        log.info("删除系统部门，deptId={}", deptId);
        SysDeptEntity entity = requireActiveDept(deptId);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        sysDeptMapper.updateById(entity);
    }

    private LambdaQueryWrapper<SysDeptEntity> buildListWrapper(String keyword) {
        LambdaQueryWrapper<SysDeptEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysDeptEntity::getDeleted);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysDeptEntity::getDeptName, keyword);
        }
        return wrapper.orderByAsc(SysDeptEntity::getSort)
            .orderByAsc(SysDeptEntity::getId);
    }

    private String resolveAncestors(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return "0";
        }
        SysDeptEntity parent = MybatisTenantHelpers.requireEntity(sysDeptMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysDeptEntity::getDeleted)
                .eq(SysDeptEntity::getStatus, CommonStatusEnum.ENABLED.getStatus())
                .eq(SysDeptEntity::getId, parentId)
                .last("limit 1")), "父部门不存在");
        return DefaultValueUtils.defaultIfBlank(parent.getAncestors(), "0") + "," + parent.getId();
    }

    /**
     * 校验部门处于可用状态。
     *
     * @param deptId 部门编号
     * @return 部门实体
     */
    private SysDeptEntity requireActiveDept(Long deptId) {
        return MybatisTenantHelpers.requireEntity(sysDeptMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysDeptEntity::getDeleted)
                .eq(SysDeptEntity::getId, deptId)
                .last("limit 1")), "部门不存在");
    }
}
