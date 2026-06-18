package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hlw.common.core.domain.PageQuery;
import com.hlw.common.core.domain.PageResult;
import com.hlw.common.core.enums.CommonStatusEnum;
import com.hlw.common.core.enums.DeletedStatusEnum;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.common.security.PasswordEncoder;
import com.hlw.system.domain.req.CreateUserReq;
import com.hlw.system.domain.resp.UserResp;
import com.hlw.system.entity.SysDeptEntity;
import com.hlw.system.entity.SysPostEntity;
import com.hlw.system.entity.SysUserEntity;
import com.hlw.system.entity.SysUserPostEntity;
import com.hlw.system.mapper.SysDeptMapper;
import com.hlw.system.mapper.SysPostMapper;
import com.hlw.system.mapper.SysUserMapper;
import com.hlw.system.mapper.SysUserPostMapper;
import com.hlw.system.service.converter.UserConverter;
import com.hlw.system.service.support.MybatisTenantHelpers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 后台用户聚合服务，负责用户的查询、创建编排。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    /** 用户数据访问组件。 */
    private final SysUserMapper sysUserMapper;
    /** 部门数据访问组件。 */
    private final SysDeptMapper sysDeptMapper;
    /** 岗位数据访问组件。 */
    private final SysPostMapper sysPostMapper;
    /** 用户岗位关联数据访问组件。 */
    private final SysUserPostMapper sysUserPostMapper;
    /** 用户展示对象转换器。 */
    private final UserConverter userConverter;

    /**
     * 分页查询用户列表，并按当前页用户补全部门、岗位名称。
     *
     * @param query 分页查询条件
     * @return 用户分页结果
     */
    @Transactional(readOnly = true)
    public PageResult<UserResp> listUsers(PageQuery query) {
        log.info("查询系统用户列表分页，pageNum={}，pageSize={}，keyword={}",
            query.getPageNum(), query.getPageSize(), query.getKeyword());

        Page<SysUserEntity> page = query.toPage();
        LambdaQueryWrapper<SysUserEntity> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(SysUserEntity::getUsername, query.getKeyword());
        }
        wrapper.orderByAsc(SysUserEntity::getId);

        Page<SysUserEntity> result = sysUserMapper.selectPage(page, wrapper);
        List<SysUserEntity> users = result.getRecords();
        Map<Long, String> deptNameMap = resolveDeptNameMap(users);
        Map<Long, String> postNameMap = resolvePostNameMap(users);

        List<UserResp> records = new ArrayList<>();
        for (SysUserEntity user : users) {
            if (user.getDeptId() != null && deptNameMap.containsKey(user.getDeptId())) {
                user.setDeptName(deptNameMap.get(user.getDeptId()));
            }
            records.add(userConverter.toUserVO(user, postNameMap.getOrDefault(user.getId(), "-")));
        }
        return new PageResult<>(records, result.getTotal(), result.getCurrent(), result.getSize());
    }

    /**
     * 创建用户。
     *
     * @param request 创建用户请求
     * @return 新建用户展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResp createUser(CreateUserReq request) {
        log.info("创建后台用户，username={}，deptId={}，deptName={}，roleName={}",
            request.getUsername(), request.getDeptId(), request.getDeptName(), request.getRoleName());
        SysDeptEntity dept = resolveCreateDept(request);
        SysUserEntity entity = new SysUserEntity();
        entity.setUsername(request.getUsername());
        String rawPassword = DefaultValueUtils.defaultIfBlank(request.getPassword(), "123456");
        entity.setPassword(PasswordEncoder.encode(rawPassword));
        entity.setPhone(DefaultValueUtils.defaultIfBlank(request.getPhone(), ""));
        entity.setUserType(DefaultValueUtils.defaultIfBlank(request.getUserType(), "ADMIN"));
        if (dept != null) {
            entity.setDeptId(dept.getId());
            entity.setDeptName(dept.getDeptName());
        } else {
            entity.setDeptName(DefaultValueUtils.defaultIfBlank(request.getDeptName(), "运营部"));
        }
        entity.setRoleName(DefaultValueUtils.defaultIfBlank(request.getRoleName(), "系统管理员"));
        entity.setLastLogin("-");
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        entity.setDeleted(0);
        sysUserMapper.insert(entity);
        return userConverter.toUserVO(entity, "-");
    }

    /**
     * 查询用户详情。
     *
     * @param userId 用户编号
     * @return 用户展示对象
     */
    @Transactional(readOnly = true)
    public UserResp getUser(Long userId) {
        log.info("查询后台用户详情，userId={}", userId);
        SysUserEntity user = requireActiveUser(userId);
        return userConverter.toUserVO(user, resolvePostName(user.getId(), resolveUserPostMap(user.getId()), resolvePostMap(user.getId())));
    }

    /**
     * 更新用户。
     *
     * @param userId 用户编号
     * @param request 用户更新请求
     * @return 更新后的用户展示对象
     */
    @Transactional(rollbackFor = Exception.class)
    public UserResp updateUser(Long userId, CreateUserReq request) {
        log.info("更新后台用户，userId={}，username={}", userId, request.getUsername());
        SysUserEntity entity = requireActiveUser(userId);
        entity.setUsername(request.getUsername());
        entity.setPhone(DefaultValueUtils.defaultIfBlank(request.getPhone(), ""));
        entity.setUserType(DefaultValueUtils.defaultIfBlank(request.getUserType(), "ADMIN"));
        entity.setDeptId(request.getDeptId());
        if (request.getDeptId() != null) {
            SysDeptEntity dept = resolveCreateDept(request);
            entity.setDeptId(dept.getId());
            entity.setDeptName(dept.getDeptName());
        } else {
            entity.setDeptName(DefaultValueUtils.defaultIfBlank(request.getDeptName(), "运营部"));
        }
        entity.setRoleName(DefaultValueUtils.defaultIfBlank(request.getRoleName(), "系统管理员"));
        entity.setStatus(DefaultValueUtils.defaultIfBlank(request.getStatus(), CommonStatusEnum.ENABLED.getStatus()));
        if (StringUtils.hasText(request.getPassword())) {
            entity.setPassword(PasswordEncoder.encode(request.getPassword()));
        }
        sysUserMapper.updateById(entity);
        return userConverter.toUserVO(entity, resolvePostName(entity.getId(), resolveUserPostMap(entity.getId()), resolvePostMap(entity.getId())));
    }

    /**
     * 删除用户。
     *
     * @param userId 用户编号
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        log.info("删除后台用户，userId={}", userId);
        SysUserEntity entity = requireActiveUser(userId);
        entity.setDeleted(DeletedStatusEnum.DELETED.getType());
        sysUserMapper.updateById(entity);
    }

    /**
     * 校验用户处于可用状态。
     *
     * @param userId 用户编号
     * @return 用户实体
     */
    private SysUserEntity requireActiveUser(Long userId) {
        return MybatisTenantHelpers.requireEntity(sysUserMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getId, userId)
            .last("limit 1")), "用户不存在");
    }

    private SysDeptEntity resolveCreateDept(CreateUserReq request) {
        if (request.getDeptId() != null) {
            return MybatisTenantHelpers.requireEntity(sysDeptMapper.selectOne(
                MybatisTenantHelpers.notDeletedWrapper(SysDeptEntity::getDeleted)
                    .eq(SysDeptEntity::getStatus, CommonStatusEnum.ENABLED.getStatus())
                    .eq(SysDeptEntity::getId, request.getDeptId())
                    .last("limit 1")), "部门不存在");
        }
        if (!StringUtils.hasText(request.getDeptName())) {
            return null;
        }
        return sysDeptMapper.selectOne(MybatisTenantHelpers.notDeletedWrapper(SysDeptEntity::getDeleted)
            .eq(SysDeptEntity::getStatus, CommonStatusEnum.ENABLED.getStatus())
            .eq(SysDeptEntity::getDeptName, request.getDeptName())
            .last("limit 1"));
    }

    private Map<Long, String> resolveDeptNameMap(List<SysUserEntity> users) {
        Set<Long> deptIds = users.stream()
            .map(SysUserEntity::getDeptId)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        if (deptIds.isEmpty()) {
            return Map.of();
        }
        return sysDeptMapper.selectList(MybatisTenantHelpers.notDeletedWrapper(SysDeptEntity::getDeleted)
                .in(SysDeptEntity::getId, deptIds)).stream()
            .collect(Collectors.toMap(SysDeptEntity::getId, SysDeptEntity::getDeptName, (left, right) -> left));
    }

    /**
     * 按当前页用户构建岗位名称查询映射，避免全表加载。
     *
     * @param users 当前页用户列表
     * @return 用户编号到岗位名称的映射
     */
    private Map<Long, String> resolvePostNameMap(List<SysUserEntity> users) {
        if (users.isEmpty()) {
            return Map.of();
        }
        List<Long> userIds = users.stream().map(SysUserEntity::getId).toList();
        LambdaQueryWrapper<SysUserPostEntity> relationWrapper = MybatisTenantHelpers.notDeletedWrapper(SysUserPostEntity::getDeleted)
            .in(SysUserPostEntity::getUserId, userIds);
        List<SysUserPostEntity> relations = sysUserPostMapper.selectList(relationWrapper);
        if (relations.isEmpty()) {
            return Map.of();
        }
        Set<Long> postIds = relations.stream().map(SysUserPostEntity::getPostId).collect(Collectors.toSet());
        Map<Long, SysPostEntity> postMap = sysPostMapper.selectList(
                MybatisTenantHelpers.notDeletedWrapper(SysPostEntity::getDeleted).in(SysPostEntity::getId, postIds)
            ).stream()
            .collect(Collectors.toMap(SysPostEntity::getId, post -> post));
        Map<Long, List<SysUserPostEntity>> userPostMap = relations.stream()
            .collect(Collectors.groupingBy(SysUserPostEntity::getUserId));
        return users.stream()
            .collect(Collectors.toMap(
                SysUserEntity::getId,
                user -> resolvePostName(user.getId(), userPostMap, postMap),
                (left, right) -> left
            ));
    }

    /**
     * 解析用户岗位名称。
     *
     * @param userId 用户编号
     * @param userPostMap 用户岗位映射
     * @param postMap 岗位映射
     * @return 岗位名称
     */
    private String resolvePostName(
        Long userId,
        Map<Long, List<SysUserPostEntity>> userPostMap,
        Map<Long, SysPostEntity> postMap
    ) {
        List<SysUserPostEntity> relations = userPostMap.getOrDefault(userId, List.of());
        if (relations.isEmpty()) {
            return "-";
        }
        Set<String> postNames = relations.stream()
            .map(relation -> postMap.get(relation.getPostId()))
            .filter(Objects::nonNull)
            .map(SysPostEntity::getPostName)
            .collect(Collectors.toCollection(LinkedHashSet::new));
        return postNames.isEmpty() ? "-" : String.join("、", postNames);
    }

    /**
     * 构建指定用户的岗位关系映射。
     *
     * @param userId 用户编号
     * @return 用户岗位关系列表
     */
    private Map<Long, List<SysUserPostEntity>> resolveUserPostMap(Long userId) {
        List<SysUserPostEntity> relations = sysUserPostMapper.selectList(
            MybatisTenantHelpers.notDeletedWrapper(SysUserPostEntity::getDeleted)
                .eq(SysUserPostEntity::getUserId, userId));
        return relations.stream().collect(Collectors.groupingBy(SysUserPostEntity::getUserId));
    }

    /**
     * 构建指定用户的岗位映射。
     *
     * @param userId 用户编号
     * @return 岗位映射
     */
    private Map<Long, SysPostEntity> resolvePostMap(Long userId) {
        List<SysUserPostEntity> relations = sysUserPostMapper.selectList(
            MybatisTenantHelpers.notDeletedWrapper(SysUserPostEntity::getDeleted)
                .eq(SysUserPostEntity::getUserId, userId));
        if (relations.isEmpty()) {
            return Map.of();
        }
        Set<Long> postIds = relations.stream().map(SysUserPostEntity::getPostId).collect(Collectors.toSet());
        return sysPostMapper.selectList(
                MybatisTenantHelpers.notDeletedWrapper(SysPostEntity::getDeleted)
                    .in(SysPostEntity::getId, postIds))
            .stream()
            .collect(Collectors.toMap(SysPostEntity::getId, post -> post));
    }
}
