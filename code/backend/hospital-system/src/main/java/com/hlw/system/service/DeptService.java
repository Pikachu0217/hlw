package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.dto.CreateDeptRequest;
import com.hlw.system.entity.SysDeptEntity;
import com.hlw.system.mapper.SysDeptMapper;
import com.hlw.system.service.converter.DeptConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.vo.DeptVO;
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
    public PageResult<DeptVO> listDepts(PageQuery query) {
        log.info("查询系统部门列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());

        Page<SysDeptEntity> page = query.toPage();
        LambdaQueryWrapper<SysDeptEntity> wrapper = buildListWrapper(query.getKeyword());
        Page<SysDeptEntity> result = sysDeptMapper.selectPage(page, wrapper);
        List<DeptVO> records = result.getRecords().stream()
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
    public List<DeptVO> listDeptOptions(PageQuery query) {
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
    public DeptVO createDept(CreateDeptRequest request) {
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
}
