package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.domain.req.CreateRoleReq;
import com.hlw.system.domain.resp.RoleResp;
import com.hlw.system.entity.SysRoleEntity;
import com.hlw.system.entity.SysUserRoleEntity;
import com.hlw.system.mapper.SysRoleMapper;
import com.hlw.system.mapper.SysUserRoleMapper;
import com.hlw.system.service.converter.RoleConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 角色聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RoleService {
    /** 角色数据访问组件。 */
    private final SysRoleMapper sysRoleMapper;
    /** 用户角色关系数据访问组件。 */
    private final SysUserRoleMapper sysUserRoleMapper;
    /** 角色展示对象转换器。 */
    private final RoleConverter roleConverter;

    /**
     * 分页查询角色列表。
     *
     * @param query 分页查询条件
     * @return 角色分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<RoleResp> listRoles(PageQuery query) {
        log.info("查询角色列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysRoleEntity> wrapper = new LambdaQueryWrapper<SysRoleEntity>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(item -> item.like(SysRoleEntity::getRoleName, query.getKeyword())
                .or()
                .like(SysRoleEntity::getRoleCode, query.getKeyword()));
        }
        wrapper.orderByAsc(SysRoleEntity::getOrderNum).orderByAsc(SysRoleEntity::getId);
        Page<SysRoleEntity> page = sysRoleMapper.selectPage(query.toPage(), wrapper);
        List<RoleResp> records = page.getRecords().stream()
            .map(role -> roleConverter.toRoleVO(role, countMembers(role.getId())))
            .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建角色。
     *
     * @param request 角色创建请求
     * @return 角色展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleResp createRole(CreateRoleReq request) {
        log.info("创建角色，roleCode={}，roleName={}", request.getRoleCode(), request.getRoleName());
        SysRoleEntity entity = new SysRoleEntity();
        fillRole(entity, request);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.insert(entity);
        return roleConverter.toRoleVO(entity, 0);
    }

    /**
     * 查询角色详情。
     *
     * @param id 角色编号
     * @return 角色展示对象
     */
    @Transactional(readOnly = true)
    public RoleResp getRole(Long id) {
        log.info("查询角色详情，id={}", id);
        SysRoleEntity role = requireRole(id);
        return roleConverter.toRoleVO(role, countMembers(id));
    }

    /**
     * 更新角色。
     *
     * @param id 角色编号
     * @param request 角色更新请求
     * @return 角色展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleResp updateRole(Long id, CreateRoleReq request) {
        log.info("更新角色，id={}，roleName={}", id, request.getRoleName());
        SysRoleEntity entity = requireRole(id);
        fillRole(entity, request);
        entity.setUpdateTime(LocalDateTime.now());
        sysRoleMapper.updateById(entity);
        return roleConverter.toRoleVO(entity, countMembers(id));
    }

    /**
     * 删除角色。
     *
     * @param id 角色编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        log.info("删除角色，id={}", id);
        requireRole(id);
        sysRoleMapper.deleteById(id);
    }

    /**
     * 查询角色实体。
     *
     * @param id 角色编号
     * @return 角色实体
     */
    @Transactional(readOnly = true)
    public SysRoleEntity requireRole(Long id) {
        return MybatisTenantHelpers.requireEntity(sysRoleMapper.selectOne(
            new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getId, id)
                .last("limit 1")), "角色不存在");
    }

    /**
     * 填充角色实体字段。
     *
     * @param entity 角色实体
     * @param request 角色请求
     */
    private void fillRole(SysRoleEntity entity, CreateRoleReq request) {
        entity.setRoleName(request.getRoleName());
        entity.setRoleCode(request.getRoleCode());
        entity.setOrderNum(DefaultValueUtils.defaultIfNull(request.getOrderNum(), 0));
        entity.setDataScope(DefaultValueUtils.defaultIfNull(request.getDataScope(), 1));
        entity.setStatus(DefaultValueUtils.defaultIfNull(request.getStatus(), 0));
        entity.setRemark(request.getRemark());
    }

    /**
     * 统计角色成员数。
     *
     * @param roleId 角色编号
     * @return 成员数
     */
    private Integer countMembers(Long roleId) {
        return Math.toIntExact(sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRoleEntity>()
            .eq(SysUserRoleEntity::getRoleId, roleId)));
    }
}
