package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.hlw.common.core.domain.system.resp.InternalDeptResp;
import com.hlw.system.constants.SystemTenantConstants;
import com.hlw.system.entity.SysDeptEntity;
import com.hlw.system.mapper.SysDeptMapper;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Supplier;

/**
 * 系统内部部门查询服务，供业务服务通过 Feign 调用。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InternalDeptService {
    /** 部门数据访问组件。 */
    private final SysDeptMapper sysDeptMapper;

    /**
     * 查询租户科室部门列表。
     *
     * @param tenantId 租户编号
     * @return 科室部门列表
     */
    @Transactional(readOnly = true)
    public List<InternalDeptResp> listDepartments(Long tenantId) {
        log.info("内部查询科室部门列表，tenantId={}", tenantId);
        return ignoreTenantLine(() -> sysDeptMapper.selectList(new LambdaQueryWrapper<SysDeptEntity>()
                .eq(SysDeptEntity::getTenantId, String.valueOf(tenantId))
                .eq(SysDeptEntity::getIsDepartment, 1)
                .eq(SysDeptEntity::getStatus, SystemTenantConstants.STATUS_NORMAL_VALUE)
                .orderByAsc(SysDeptEntity::getOrderNum)
                .orderByAsc(SysDeptEntity::getId))).stream()
            .map(this::toInternalDeptResp)
            .toList();
    }

    /**
     * 查询租户部门详情。
     *
     * @param id 部门编号
     * @param tenantId 租户编号
     * @return 部门详情
     */
    @Transactional(readOnly = true)
    public InternalDeptResp getDept(Long id, Long tenantId) {
        log.info("内部查询部门详情，id={}，tenantId={}", id, tenantId);
        SysDeptEntity entity = ignoreTenantLine(() -> sysDeptMapper.selectOne(new LambdaQueryWrapper<SysDeptEntity>()
            .eq(SysDeptEntity::getId, id)
            .eq(SysDeptEntity::getTenantId, String.valueOf(tenantId))
            .last("limit 1")));
        return entity == null ? null : toInternalDeptResp(entity);
    }

    /**
     * 转换内部部门展示对象。
     *
     * @param entity 部门实体
     * @return 内部部门展示对象
     */
    private InternalDeptResp toInternalDeptResp(SysDeptEntity entity) {
        InternalDeptResp resp = new InternalDeptResp();
        resp.setId(entity.getId());
        resp.setTenantId(parseTenantId(entity.getTenantId()));
        resp.setParentId(entity.getParentId());
        resp.setDeptName(entity.getDeptName());
        resp.setIsDepartment(entity.getIsDepartment());
        resp.setAncestors(entity.getAncestors());
        resp.setOrderNum(entity.getOrderNum());
        resp.setStatus(entity.getStatus());
        return resp;
    }

    /**
     * 解析租户编号。
     *
     * @param tenantId 租户编号字符串
     * @return 租户编号
     */
    private Long parseTenantId(String tenantId) {
        try {
            return Long.parseLong(tenantId);
        } catch (NumberFormatException exception) {
            log.warn("部门租户编号无法转换为 Long，tenantId={}", tenantId);
            return -1L;
        }
    }

    /**
     * 执行显式租户条件查询并跳过 MyBatis Plus 自动租户拼接。
     *
     * @param supplier 查询执行器
     * @param <T> 返回值类型
     * @return 查询返回值
     */
    private <T> T ignoreTenantLine(Supplier<T> supplier) {
        return InterceptorIgnoreHelper.execute(MybatisTenantHelpers.ignoreTenantLine(), supplier);
    }
}
