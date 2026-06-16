package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.system.dto.CreateRoleRequest;
import com.hlw.system.entity.SysRoleEntity;
import com.hlw.system.entity.SysUserRoleEntity;
import com.hlw.system.mapper.SysRoleMapper;
import com.hlw.system.mapper.SysUserRoleMapper;
import com.hlw.system.service.converter.RoleConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.vo.RoleVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色聚合服务，负责角色的查询与创建编排，并统计成员数量。
 */
@Service
@RequiredArgsConstructor
public class RoleService {
    private static final Logger log = LoggerFactory.getLogger(RoleService.class);
    private static final String DEFAULT_DATA_SCOPE = "本租户数据";

    /** 角色数据访问组件。 */
    private final SysRoleMapper sysRoleMapper;
    /** 用户角色关联数据访问组件。 */
    private final SysUserRoleMapper sysUserRoleMapper;
    /** 角色展示对象转换器。 */
    private final RoleConverter roleConverter;

    /**
     * 分页查询角色列表，并按当前页角色统计成员数量。
     *
     * @param query 分页查询条件
     * @return 角色分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<RoleVO> listRoles(PageQuery query) {
        log.info("查询系统角色列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());

        Page<SysRoleEntity> page = query.toPage();
        LambdaQueryWrapper<SysRoleEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysRoleEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysRoleEntity::getRoleName, query.getKeyword());
        }
        wrapper.orderByAsc(SysRoleEntity::getId);

        Page<SysRoleEntity> result = sysRoleMapper.selectPage(page, wrapper);
        List<RoleVO> records = result.getRecords().stream()
            .map(role -> roleConverter.toRoleVO(role, countRoleMembers(role.getId())))
            .toList();
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 创建角色。
     *
     * @param request 创建角色请求
     * @return 新建角色展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public RoleVO createRole(CreateRoleRequest request) {
        log.info("创建角色，roleName={}，roleCode={}", request.getRoleName(), request.getRoleCode());
        SysRoleEntity entity = new SysRoleEntity();
        entity.setRoleName(request.getRoleName());
        entity.setRoleCode(request.getRoleCode());
        entity.setDataScope(DefaultValueUtils.defaultIfBlank(request.getDataScope(), DEFAULT_DATA_SCOPE));
        entity.setMemberCount(0);
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setDeleted(0);
        sysRoleMapper.insert(entity);
        return roleConverter.toRoleVO(entity, 0);
    }

    /**
     * 统计指定角色绑定的成员数量。
     *
     * @param roleId 角色编号
     * @return 成员数量
     */
    private Integer countRoleMembers(Long roleId) {
        Long count = sysUserRoleMapper.selectCount(MybatisTenantHelpers.notDeletedWrapper(SysUserRoleEntity::getDeleted)
            .eq(SysUserRoleEntity::getRoleId, roleId));
        return count == null ? 0 : count.intValue();
    }

    /**
     * 校验角色处于可用状态。
     *
     * @param roleId 角色编号
     * @return 角色实体
     */
    private SysRoleEntity requireActiveRole(Long roleId) {
        return MybatisTenantHelpers.requireEntity(sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRoleEntity>()
            .eq(SysRoleEntity::getDeleted, 0)
            .eq(SysRoleEntity::getId, roleId)
            .last("limit 1")), "角色不存在");
    }
}
