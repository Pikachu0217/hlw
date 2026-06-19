package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateDeptReq;
import com.hlw.system.domain.resp.DeptResp;
import com.hlw.system.entity.SysDeptEntity;
import com.hlw.system.mapper.SysDeptMapper;
import com.hlw.system.service.converter.DeptConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门聚合服务。
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
     * 分页查询部门列表。
     *
     * @param query 分页查询条件
     * @return 部门分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<DeptResp> listDepts(PageQuery query) {
        log.info("查询部门列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysDeptEntity> wrapper = buildListWrapper(query);
        Page<SysDeptEntity> page = sysDeptMapper.selectPage(query.toPage(), wrapper);
        List<DeptResp> records = page.getRecords().stream().map(deptConverter::toDeptVO).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 查询部门选项。
     *
     * @param query 查询条件
     * @return 部门选项列表
     */
    @Transactional(readOnly = true)
    public List<DeptResp> listDeptOptions(PageQuery query) {
        log.info("查询部门选项，keyword={}", query.getKeyword());
        return sysDeptMapper.selectList(buildListWrapper(query)).stream().map(deptConverter::toDeptVO).toList();
    }

    /**
     * 创建部门。
     *
     * @param request 部门创建请求
     * @return 部门展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public DeptResp createDept(CreateDeptReq request) {
        log.info("创建部门，deptName={}，parentId={}", request.getDeptName(), request.getParentId());
        SysDeptEntity entity = new SysDeptEntity();
        fillDept(entity, request);
        entity.setAncestors(resolveAncestors(entity.getParentId()));
        entity.setDeleted(DeletedStatusEnum.NOT_DELETED.getType());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysDeptMapper.insert(entity);
        return deptConverter.toDeptVO(entity);
    }

    /**
     * 查询部门详情。
     *
     * @param id 部门编号
     * @return 部门展示对象
     */
    @Transactional(readOnly = true)
    public DeptResp getDept(Long id) {
        log.info("查询部门详情，id={}", id);
        return deptConverter.toDeptVO(requireDept(id));
    }

    /**
     * 更新部门。
     *
     * @param id 部门编号
     * @param request 部门更新请求
     * @return 部门展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public DeptResp updateDept(Long id, CreateDeptReq request) {
        log.info("更新部门，id={}，deptName={}", id, request.getDeptName());
        SysDeptEntity entity = requireDept(id);
        fillDept(entity, request);
        entity.setAncestors(resolveAncestors(entity.getParentId()));
        entity.setUpdateTime(LocalDateTime.now());
        sysDeptMapper.updateById(entity);
        return deptConverter.toDeptVO(entity);
    }

    /**
     * 删除部门。
     *
     * @param id 部门编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteDept(Long id) {
        log.info("删除部门，id={}", id);
        SysDeptEntity entity = requireDept(id);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        entity.setUpdateTime(LocalDateTime.now());
        sysDeptMapper.updateById(entity);
    }

    /**
     * 构造部门列表查询条件。
     *
     * @param query 查询条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<SysDeptEntity> buildListWrapper(PageQuery query) {
        LambdaQueryWrapper<SysDeptEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysDeptEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysDeptEntity::getDeptName, query.getKeyword());
        }
        return wrapper.orderByAsc(SysDeptEntity::getParentId).orderByAsc(SysDeptEntity::getOrderNum).orderByAsc(SysDeptEntity::getId);
    }

    /**
     * 填充部门实体字段。
     *
     * @param entity 部门实体
     * @param request 部门请求
     */
    private void fillDept(SysDeptEntity entity, CreateDeptReq request) {
        entity.setParentId(DefaultValueUtils.defaultIfNull(request.getParentId(), 0L));
        entity.setDeptName(request.getDeptName());
        entity.setOrderNum(DefaultValueUtils.defaultIfNull(request.getOrderNum(), 0));
        entity.setLeader(request.getLeader());
        entity.setPhone(request.getPhone());
        entity.setEmail(request.getEmail());
        entity.setStatus(DefaultValueUtils.defaultIfNull(request.getStatus(), 0));
    }

    /**
     * 解析祖级列表。
     *
     * @param parentId 父部门编号
     * @return 祖级列表
     */
    private String resolveAncestors(Long parentId) {
        if (parentId == null || parentId == 0L) {
            return "0";
        }
        SysDeptEntity parent = requireDept(parentId);
        return parent.getAncestors() + "," + parent.getId();
    }

    /**
     * 校验部门存在。
     *
     * @param id 部门编号
     * @return 部门实体
     */
    private SysDeptEntity requireDept(Long id) {
        return MybatisTenantHelpers.requireEntity(sysDeptMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysDeptEntity::getDeleted)
                .eq(SysDeptEntity::getId, id)
                .last("limit 1")), "部门不存在");
    }
}
