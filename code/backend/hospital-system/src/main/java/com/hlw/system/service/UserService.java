package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.common.security.PasswordEncoder;
import com.hlw.system.domain.req.CreateUserReq;
import com.hlw.system.domain.resp.UserResp;
import com.hlw.system.entity.SysDeptEntity;
import com.hlw.system.entity.SysPostEntity;
import com.hlw.system.entity.SysRoleEntity;
import com.hlw.system.entity.SysUserEntity;
import com.hlw.system.entity.SysUserPostEntity;
import com.hlw.system.entity.SysUserRoleEntity;
import com.hlw.system.mapper.SysDeptMapper;
import com.hlw.system.mapper.SysPostMapper;
import com.hlw.system.mapper.SysRoleMapper;
import com.hlw.system.mapper.SysUserMapper;
import com.hlw.system.mapper.SysUserPostMapper;
import com.hlw.system.mapper.SysUserRoleMapper;
import com.hlw.system.service.converter.UserConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.service.support.UserIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 后台用户聚合服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    /** 用户数据访问组件。 */
    private final SysUserMapper sysUserMapper;
    /** 部门数据访问组件。 */
    private final SysDeptMapper sysDeptMapper;
    /** 用户岗位关系数据访问组件。 */
    private final SysUserPostMapper sysUserPostMapper;
    /** 岗位数据访问组件。 */
    private final SysPostMapper sysPostMapper;
    /** 用户角色关系数据访问组件。 */
    private final SysUserRoleMapper sysUserRoleMapper;
    /** 角色数据访问组件。 */
    private final SysRoleMapper sysRoleMapper;
    /** 用户展示对象转换器。 */
    private final UserConverter userConverter;

    /**
     * 分页查询用户列表。
     *
     * @param query 分页查询条件
     * @return 用户分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<UserResp> listUsers(PageQuery query) {
        log.info("查询系统用户列表，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());
        LambdaQueryWrapper<SysUserEntity> wrapper = MybatisTenantHelpers.notDeletedWrapper(SysUserEntity::getDeleted);
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(item -> item.like(SysUserEntity::getUserName, query.getKeyword())
                .or()
                .like(SysUserEntity::getNickName, query.getKeyword())
                .or()
                .like(SysUserEntity::getPhone, query.getKeyword()));
        }
        wrapper.orderByAsc(SysUserEntity::getId);
        Page<SysUserEntity> page = sysUserMapper.selectPage(query.toPage(), wrapper);
        Map<Long, String> deptNameMap = resolveDeptNameMap(page.getRecords());
        Map<String, String> postNameMap = resolvePostNameMap(page.getRecords());
        Map<String, String> roleNameMap = resolveRoleNameMap(page.getRecords());
        List<UserResp> records = page.getRecords().stream()
            .map(user -> enrichUser(userConverter.toUserVO(user, postNameMap.getOrDefault(user.getUserId(), "-")),
                deptNameMap.getOrDefault(user.getDeptId(), "-"),
                roleNameMap.getOrDefault(user.getUserId(), "-")))
            .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    /**
     * 创建系统用户。
     *
     * @param request 用户创建请求
     * @return 用户展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResp createUser(CreateUserReq request) {
        log.info("创建系统用户，userName={}，deptId={}", request.getUserName(), request.getDeptId());
        SysUserEntity entity = new SysUserEntity();
        entity.setUserId(UserIdGenerator.nextUserId());
        fillUser(entity, request, true);
        entity.setDeleted(DeletedStatusEnum.NOT_DELETED.getType());
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        sysUserMapper.insert(entity);
        UserResp resp = userConverter.toUserVO(entity, "-");
        return enrichUser(resp, resolveDeptName(entity.getDeptId()), "-");
    }

    /**
     * 查询用户详情。
     *
     * @param id 用户表主键
     * @return 用户展示对象
     */
    @Transactional(readOnly = true)
    public UserResp getUser(Long id) {
        log.info("查询系统用户详情，id={}", id);
        SysUserEntity entity = requireUser(id);
        UserResp resp = userConverter.toUserVO(entity, resolvePostName(entity.getUserId()));
        return enrichUser(resp, resolveDeptName(entity.getDeptId()), resolveRoleName(entity.getUserId()));
    }

    /**
     * 更新系统用户。
     *
     * @param id 用户表主键
     * @param request 用户更新请求
     * @return 用户展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResp updateUser(Long id, CreateUserReq request) {
        log.info("更新系统用户，id={}，userName={}", id, request.getUserName());
        SysUserEntity entity = requireUser(id);
        fillUser(entity, request, false);
        entity.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(entity);
        UserResp resp = userConverter.toUserVO(entity, resolvePostName(entity.getUserId()));
        return enrichUser(resp, resolveDeptName(entity.getDeptId()), resolveRoleName(entity.getUserId()));
    }

    /**
     * 删除系统用户。
     *
     * @param id 用户表主键
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long id) {
        log.info("删除系统用户，id={}", id);
        SysUserEntity entity = requireUser(id);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        entity.setUpdateTime(LocalDateTime.now());
        sysUserMapper.updateById(entity);
    }

    /**
     * 按表主键查询用户实体。
     *
     * @param id 用户表主键
     * @return 用户实体
     */
    @Transactional(readOnly = true)
    public SysUserEntity requireUser(Long id) {
        return MybatisTenantHelpers.requireEntity(sysUserMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysUserEntity::getDeleted)
                .eq(SysUserEntity::getId, id)
                .last("limit 1")), "用户不存在");
    }

    /**
     * 按业务用户编号查询用户实体。
     *
     * @param userId 业务用户编号
     * @return 用户实体
     */
    @Transactional(readOnly = true)
    public SysUserEntity requireUserByUserId(String userId) {
        return MybatisTenantHelpers.requireEntity(sysUserMapper.selectOne(
            MybatisTenantHelpers.notDeletedWrapper(SysUserEntity::getDeleted)
                .eq(SysUserEntity::getUserId, userId)
                .last("limit 1")), "用户不存在");
    }

    /**
     * 填充用户实体字段。
     *
     * @param entity 用户实体
     * @param request 用户请求
     * @param create 是否创建场景
     */
    private void fillUser(SysUserEntity entity, CreateUserReq request, boolean create) {
        entity.setUserName(request.getUserName());
        entity.setNickName(DefaultValueUtils.defaultIfBlank(request.getNickName(), request.getUserName()));
        entity.setDeptId(request.getDeptId());
        entity.setUserType(DefaultValueUtils.defaultIfBlank(request.getUserType(), "sys_user"));
        entity.setEmail(DefaultValueUtils.defaultIfBlank(request.getEmail(), ""));
        entity.setPhone(DefaultValueUtils.defaultIfBlank(request.getPhone(), ""));
        entity.setSex(DefaultValueUtils.defaultIfBlank(request.getSex(), "2"));
        entity.setStatus(DefaultValueUtils.defaultIfNull(request.getStatus(), 0));
        entity.setRemark(request.getRemark());
        if (create || StringUtils.hasText(request.getPassword())) {
            entity.setPassword(PasswordEncoder.encode(DefaultValueUtils.defaultIfBlank(request.getPassword(), "123456")));
        }
    }

    /**
     * 补充用户展示信息。
     *
     * @param resp 用户展示对象
     * @param deptName 部门名称
     * @param roleName 角色名称
     * @return 用户展示对象
     */
    private UserResp enrichUser(UserResp resp, String deptName, String roleName) {
        resp.setDeptName(deptName);
        resp.setRoleName(roleName);
        return resp;
    }

    /**
     * 构建部门名称映射。
     *
     * @param users 用户列表
     * @return 部门名称映射
     */
    private Map<Long, String> resolveDeptNameMap(List<SysUserEntity> users) {
        Set<Long> deptIds = users.stream().map(SysUserEntity::getDeptId).filter(Objects::nonNull).collect(Collectors.toSet());
        if (deptIds.isEmpty()) {
            return Map.of();
        }
        return sysDeptMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysDeptEntity::getDeleted)
                .in(SysDeptEntity::getId, deptIds)).stream()
            .collect(Collectors.toMap(SysDeptEntity::getId, SysDeptEntity::getDeptName, (left, right) -> left));
    }

    /**
     * 解析部门名称。
     *
     * @param deptId 部门编号
     * @return 部门名称
     */
    private String resolveDeptName(Long deptId) {
        if (deptId == null) {
            return "-";
        }
        SysDeptEntity dept = sysDeptMapper.selectById(deptId);
        return dept == null ? "-" : dept.getDeptName();
    }

    /**
     * 构建岗位名称映射。
     *
     * @param users 用户列表
     * @return 岗位名称映射
     */
    private Map<String, String> resolvePostNameMap(List<SysUserEntity> users) {
        List<String> userIds = users.stream().map(SysUserEntity::getUserId).toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUserPostEntity> relations = sysUserPostMapper.selectList(new LambdaQueryWrapper<SysUserPostEntity>().in(SysUserPostEntity::getUserId, userIds));
        if (relations.isEmpty()) {
            return Map.of();
        }
        Set<Long> postIds = relations.stream().map(SysUserPostEntity::getPostId).collect(Collectors.toSet());
        Map<Long, String> postMap = sysPostMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysPostEntity::getDeleted)
                .in(SysPostEntity::getId, postIds)).stream()
            .collect(Collectors.toMap(SysPostEntity::getId, SysPostEntity::getPostName, (left, right) -> left));
        return relations.stream().collect(Collectors.groupingBy(SysUserPostEntity::getUserId,
            Collectors.mapping(item -> postMap.getOrDefault(item.getPostId(), "-"), Collectors.joining(","))));
    }

    /**
     * 解析岗位名称。
     *
     * @param userId 用户业务编号
     * @return 岗位名称
     */
    private String resolvePostName(String userId) {
        return resolvePostNameMap(List.of(requireUserByUserId(userId))).getOrDefault(userId, "-");
    }

    /**
     * 构建角色名称映射。
     *
     * @param users 用户列表
     * @return 角色名称映射
     */
    private Map<String, String> resolveRoleNameMap(List<SysUserEntity> users) {
        List<String> userIds = users.stream().map(SysUserEntity::getUserId).toList();
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<SysUserRoleEntity> relations = sysUserRoleMapper.selectList(new LambdaQueryWrapper<SysUserRoleEntity>().in(SysUserRoleEntity::getUserId, userIds));
        if (relations.isEmpty()) {
            return Map.of();
        }
        Set<Long> roleIds = relations.stream().map(SysUserRoleEntity::getRoleId).collect(Collectors.toSet());
        Map<Long, String> roleMap = sysRoleMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysRoleEntity::getDeleted)
                .in(SysRoleEntity::getId, roleIds)).stream()
            .collect(Collectors.toMap(SysRoleEntity::getId, SysRoleEntity::getRoleName, (left, right) -> left));
        return relations.stream().collect(Collectors.groupingBy(SysUserRoleEntity::getUserId,
            Collectors.mapping(item -> roleMap.getOrDefault(item.getRoleId(), "-"), Collectors.joining(","))));
    }

    /**
     * 解析角色名称。
     *
     * @param userId 用户业务编号
     * @return 角色名称
     */
    private String resolveRoleName(String userId) {
        return resolveRoleNameMap(List.of(requireUserByUserId(userId))).getOrDefault(userId, "-");
    }
}
