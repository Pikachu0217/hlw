package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.hlw.common.core.enums.HttpStatusEnum;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.common.security.PasswordEncoder;
import com.hlw.system.constants.SystemTenantConstants;
import com.hlw.system.entity.SysDeptEntity;
import com.hlw.system.entity.SysDictDataEntity;
import com.hlw.system.entity.SysDictTypeEntity;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.entity.SysPostEntity;
import com.hlw.system.entity.SysRoleEntity;
import com.hlw.system.entity.SysRoleMenuEntity;
import com.hlw.system.entity.SysTenantEntity;
import com.hlw.system.entity.SysTenantPackageEntity;
import com.hlw.system.entity.SysTenantPackageMenuEntity;
import com.hlw.system.entity.SysUserEntity;
import com.hlw.system.entity.SysUserPostEntity;
import com.hlw.system.entity.SysUserRoleEntity;
import com.hlw.system.mapper.SysDeptMapper;
import com.hlw.system.mapper.SysDictDataMapper;
import com.hlw.system.mapper.SysDictTypeMapper;
import com.hlw.system.mapper.SysMenuMapper;
import com.hlw.system.mapper.SysPostMapper;
import com.hlw.system.mapper.SysRoleMapper;
import com.hlw.system.mapper.SysRoleMenuMapper;
import com.hlw.system.mapper.SysTenantPackageMapper;
import com.hlw.system.mapper.SysTenantPackageMenuMapper;
import com.hlw.system.mapper.SysUserMapper;
import com.hlw.system.mapper.SysUserPostMapper;
import com.hlw.system.mapper.SysUserRoleMapper;
import com.hlw.system.service.support.MybatisTenantHelpers;
import com.hlw.system.service.support.SystemDefaultDataGuard;
import com.hlw.system.service.support.UserIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 新租户菜单、角色、权限和管理员初始化服务。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TenantBootstrapService {
    /** 菜单数据访问组件。 */
    private final SysMenuMapper sysMenuMapper;
    /** 租户套餐数据访问组件。 */
    private final SysTenantPackageMapper sysTenantPackageMapper;
    /** 租户套餐菜单关系数据访问组件。 */
    private final SysTenantPackageMenuMapper sysTenantPackageMenuMapper;
    /** 部门数据访问组件。 */
    private final SysDeptMapper sysDeptMapper;
    /** 字典类型数据访问组件。 */
    private final SysDictTypeMapper sysDictTypeMapper;
    /** 字典数据访问组件。 */
    private final SysDictDataMapper sysDictDataMapper;
    /** 岗位数据访问组件。 */
    private final SysPostMapper sysPostMapper;
    /** 角色数据访问组件。 */
    private final SysRoleMapper sysRoleMapper;
    /** 角色菜单关系数据访问组件。 */
    private final SysRoleMenuMapper sysRoleMenuMapper;
    /** 用户数据访问组件。 */
    private final SysUserMapper sysUserMapper;
    /** 用户岗位关系数据访问组件。 */
    private final SysUserPostMapper sysUserPostMapper;
    /** 用户角色关系数据访问组件。 */
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 初始化新租户的菜单、默认角色、管理员账号和授权关系。
     *
     * @param tenant 租户实体
     */
    public void initializeTenant(SysTenantEntity tenant) {
        initializeTenantData(tenant, true, "初始化");
    }

    /**
     * 重建租户套餐菜单和权限绑定。
     *
     * @param tenant 租户实体
     */
    public void rebuildTenantPackageBindings(SysTenantEntity tenant) {
        log.info("开始重建租户套餐权限绑定，tenantId={}，packageId={}", tenant.getTenantId(), tenant.getPackageId());
        clearTenantPackageBindings(tenant.getTenantId());
        initializeTenantData(tenant, false, "重建");
        log.info("租户套餐权限绑定重建完成，tenantId={}，packageId={}", tenant.getTenantId(), tenant.getPackageId());
    }

    /**
     * 初始化或重建租户权限基础数据。
     *
     * @param tenant 租户实体
     * @param resetAdminUserRoles 是否覆盖管理员用户角色
     * @param actionName 操作名称
     */
    private void initializeTenantData(SysTenantEntity tenant, boolean resetAdminUserRoles, String actionName) {
        if (tenant.getPackageId() == null) {
            log.warn("租户初始化失败，租户未绑定套餐，tenantId={}", tenant.getTenantId());
            throw new BizException(HttpStatusEnum.TENANT_PACKAGE_REQUIRED);
        }
        log.info("开始{}租户权限数据，tenantId={}，packageId={}", actionName, tenant.getTenantId(), tenant.getPackageId());
        SysUserEntity adminUser = upsertTenantAdminUser(tenant);
        SysDeptEntity tenantDept = upsertTenantDept(tenant, adminUser);
        SysPostEntity tenantPost = upsertTenantPost(tenant.getTenantId());
        bindAdminOrganization(tenant.getTenantId(), adminUser, tenantDept, tenantPost);
        syncDefaultDicts(tenant.getTenantId());
        List<SysMenuEntity> templateMenus = loadPackageTemplateMenus(tenant.getPackageId());
        Map<Long, SysMenuEntity> tenantMenuMap = copyPackageMenus(tenant.getTenantId(), templateMenus);
        SysRoleEntity adminRole = upsertRole(tenant.getTenantId(),
            SystemTenantConstants.TENANT_ADMIN_ROLE_CODE, SystemTenantConstants.TENANT_ADMIN_ROLE_NAME,
            SystemTenantConstants.TENANT_ADMIN_ROLE_ORDER);
        SysRoleEntity userRole = upsertRole(tenant.getTenantId(),
            SystemTenantConstants.TENANT_USER_ROLE_CODE, SystemTenantConstants.TENANT_USER_ROLE_NAME,
            SystemTenantConstants.TENANT_USER_ROLE_ORDER);
        bindRoleMenus(tenant.getTenantId(), adminRole.getId(), tenantMenuMap.values().stream().map(SysMenuEntity::getId).toList());
        bindRoleMenus(tenant.getTenantId(), userRole.getId(), resolveTenantUserMenuIds(templateMenus, tenantMenuMap));
        if (resetAdminUserRoles) {
            bindUserRole(tenant.getTenantId(), adminUser.getUserId(), adminRole.getId());
        } else {
            ensureUserRole(tenant.getTenantId(), adminUser.getUserId(), adminRole.getId());
        }
        log.info("租户权限数据{}完成，tenantId={}，deptId={}，postId={}，menuCount={}，adminRoleId={}，userRoleId={}，adminUserId={}",
            actionName, tenant.getTenantId(), tenantDept.getId(), tenantPost.getId(), tenantMenuMap.size(), adminRole.getId(), userRole.getId(), adminUser.getUserId());
    }

    /**
     * 同步平台默认字典到租户。
     *
     * @param tenantId 租户编号
     */
    private void syncDefaultDicts(String tenantId) {
        ignoreTenantLine(() -> {
            List<SysDictTypeEntity> typeTemplates = sysDictTypeMapper.selectList(new LambdaQueryWrapper<SysDictTypeEntity>()
                .eq(SysDictTypeEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID)
                .eq(SysDictTypeEntity::getIsDefault, SystemTenantConstants.SYSTEM_DEFAULT_DATA_FLAG));
            for (SysDictTypeEntity template : typeTemplates) {
                upsertTenantDictType(tenantId, template);
            }

            List<SysDictDataEntity> dataTemplates = sysDictDataMapper.selectList(new LambdaQueryWrapper<SysDictDataEntity>()
                .eq(SysDictDataEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID)
                .eq(SysDictDataEntity::getIsDefault, SystemTenantConstants.SYSTEM_DEFAULT_DATA_FLAG));
            for (SysDictDataEntity template : dataTemplates) {
                upsertTenantDictData(tenantId, template);
            }
            log.info("同步租户默认字典完成，tenantId={}，typeCount={}，dataCount={}", tenantId, typeTemplates.size(), dataTemplates.size());
            return null;
        });
    }

    /**
     * 创建或更新租户字典类型。
     *
     * @param tenantId 租户编号
     * @param template 平台字典类型模板
     */
    private void upsertTenantDictType(String tenantId, SysDictTypeEntity template) {
        SysDictTypeEntity entity = sysDictTypeMapper.selectOne(new LambdaQueryWrapper<SysDictTypeEntity>()
            .eq(SysDictTypeEntity::getTenantId, tenantId)
            .eq(SysDictTypeEntity::getDictType, template.getDictType())
            .last("limit 1"));
        boolean create = entity == null;
        if (create) {
            entity = new SysDictTypeEntity();
            entity.setTenantId(tenantId);
            entity.setDictType(template.getDictType());
            entity.setCreateTime(LocalDateTime.now());
        }
        entity.setDictName(template.getDictName());
        entity.setRemark(template.getRemark());
        entity.setIsDefault(SystemTenantConstants.SYSTEM_DEFAULT_DATA_FLAG);
        entity.setUpdateTime(LocalDateTime.now());
        if (create) {
            sysDictTypeMapper.insert(entity);
            return;
        }
        sysDictTypeMapper.updateById(entity);
    }

    /**
     * 创建或更新租户字典数据。
     *
     * @param tenantId 租户编号
     * @param template 平台字典数据模板
     */
    private void upsertTenantDictData(String tenantId, SysDictDataEntity template) {
        SysDictDataEntity entity = sysDictDataMapper.selectOne(new LambdaQueryWrapper<SysDictDataEntity>()
            .eq(SysDictDataEntity::getTenantId, tenantId)
            .eq(SysDictDataEntity::getDictType, template.getDictType())
            .eq(SysDictDataEntity::getDictValue, template.getDictValue())
            .last("limit 1"));
        boolean create = entity == null;
        if (create) {
            entity = new SysDictDataEntity();
            entity.setTenantId(tenantId);
            entity.setDictType(template.getDictType());
            entity.setDictValue(template.getDictValue());
            entity.setCreateTime(LocalDateTime.now());
        }
        entity.setDictSort(template.getDictSort());
        entity.setDictLabel(template.getDictLabel());
        entity.setRemark(template.getRemark());
        entity.setIsDefault(SystemTenantConstants.SYSTEM_DEFAULT_DATA_FLAG);
        entity.setUpdateTime(LocalDateTime.now());
        if (create) {
            sysDictDataMapper.insert(entity);
            return;
        }
        sysDictDataMapper.updateById(entity);
    }

    /**
     * 清理租户全部菜单和角色菜单绑定。
     *
     * @param tenantId 租户编号
     */
    private void clearTenantPackageBindings(String tenantId) {
        ignoreTenantLine(() -> {
            int roleMenuCount = sysRoleMenuMapper.physicalDeleteByTenantId(tenantId);
            int menuCount = sysMenuMapper.physicalDeleteByTenantId(tenantId);
            log.info("清理租户菜单和角色菜单绑定，tenantId={}，roleMenuCount={}，menuCount={}", tenantId, roleMenuCount, menuCount);
            return null;
        });
    }

    /**
     * 创建或更新租户管理员账号。
     *
     * @param tenant 租户实体
     * @return 租户管理员用户实体
     */
    private SysUserEntity upsertTenantAdminUser(SysTenantEntity tenant) {
        return ignoreTenantLine(() -> {
            SysUserEntity existing = sysUserMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
                .eq(SysUserEntity::getTenantId, tenant.getTenantId())
                .eq(SysUserEntity::getUserName, SystemTenantConstants.DEFAULT_TENANT_ADMIN_USER_NAME)
                .last("limit 1"));
            if (existing != null) {
                existing.setRealName(resolveAdminNickName(tenant));
                existing.setNickName(resolveAdminNickName(tenant));
                existing.setPhone(DefaultValueUtils.defaultIfBlank(tenant.getContactPhone(), ""));
                existing.setStatus(SystemTenantConstants.STATUS_NORMAL_VALUE);
                existing.setUpdateTime(LocalDateTime.now());
                sysUserMapper.updateById(existing);
                log.info("复用租户管理员账号，tenantId={}，userId={}", tenant.getTenantId(), existing.getUserId());
                return existing;
            }
            SysUserEntity user = new SysUserEntity();
            user.setTenantId(tenant.getTenantId());
            user.setUserId(UserIdGenerator.nextUserId());
            user.setUserName(SystemTenantConstants.DEFAULT_TENANT_ADMIN_USER_NAME);
            user.setRealName(resolveAdminNickName(tenant));
            user.setNickName(resolveAdminNickName(tenant));
            user.setUserType(SystemTenantConstants.DEFAULT_USER_TYPE);
            user.setEmail("");
            user.setPhone(DefaultValueUtils.defaultIfBlank(tenant.getContactPhone(), ""));
            user.setSex(SystemTenantConstants.DEFAULT_UNKNOWN_SEX);
            user.setPassword(PasswordEncoder.encode(SystemTenantConstants.DEFAULT_TENANT_ADMIN_PASSWORD));
            user.setStatus(SystemTenantConstants.STATUS_NORMAL_VALUE);
            user.setRemark("租户初始化默认管理员");
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());
            sysUserMapper.insert(user);
            log.info("创建租户管理员账号，tenantId={}，userId={}", tenant.getTenantId(), user.getUserId());
            return user;
        });
    }

    /**
     * 创建或更新租户默认部门。
     *
     * @param tenant 租户实体
     * @param adminUser 租户管理员用户
     * @return 租户部门实体
     */
    private SysDeptEntity upsertTenantDept(SysTenantEntity tenant, SysUserEntity adminUser) {
        return ignoreTenantLine(() -> {
            SysDeptEntity template = loadPlatformDeptTemplate();
            String deptName = resolveDeptName(template);
            SysDeptEntity dept = sysDeptMapper.selectOne(new LambdaQueryWrapper<SysDeptEntity>()
                .eq(SysDeptEntity::getTenantId, tenant.getTenantId())
                .eq(SysDeptEntity::getParentId, SystemTenantConstants.ROOT_MENU_PARENT_ID)
                .eq(SysDeptEntity::getDeptName, deptName)
                .last("limit 1"));
            boolean create = dept == null;
            if (create) {
                dept = new SysDeptEntity();
                dept.setTenantId(tenant.getTenantId());
                dept.setParentId(SystemTenantConstants.ROOT_MENU_PARENT_ID);
                dept.setCreateTime(LocalDateTime.now());
            }
            dept.setAncestors(resolveDeptAncestors(template));
            dept.setDeptName(deptName);
            dept.setOrderNum(resolveDeptOrder(template));
            dept.setLeader(adminUser.getUserId());
            dept.setPhone(resolveDeptPhone(tenant, template));
            dept.setEmail(resolveDeptEmail(template));
            dept.setStatus(SystemTenantConstants.STATUS_NORMAL_VALUE);
            dept.setUpdateTime(LocalDateTime.now());
            if (create) {
                sysDeptMapper.insert(dept);
                log.info("创建租户默认部门，tenantId={}，deptId={}，deptName={}", tenant.getTenantId(), dept.getId(), dept.getDeptName());
            } else {
                sysDeptMapper.updateById(dept);
                log.info("复用租户默认部门，tenantId={}，deptId={}，deptName={}", tenant.getTenantId(), dept.getId(), dept.getDeptName());
            }
            return dept;
        });
    }

    /**
     * 创建或更新租户默认岗位。
     *
     * @param tenantId 租户编号
     * @return 租户岗位实体
     */
    private SysPostEntity upsertTenantPost(String tenantId) {
        return ignoreTenantLine(() -> {
            SysPostEntity template = loadPlatformPostTemplate();
            String postCode = resolvePostCode(template);
            SysPostEntity post = sysPostMapper.selectOne(new LambdaQueryWrapper<SysPostEntity>()
                .eq(SysPostEntity::getTenantId, tenantId)
                .eq(SysPostEntity::getPostCode, postCode)
                .last("limit 1"));
            boolean create = post == null;
            if (create) {
                post = new SysPostEntity();
                post.setTenantId(tenantId);
                post.setPostCode(postCode);
                post.setCreateTime(LocalDateTime.now());
            }
            post.setPostName(resolvePostName(template));
            post.setOrderNum(resolvePostOrder(template));
            post.setRemark(resolvePostRemark(template));
            post.setStatus(SystemTenantConstants.STATUS_NORMAL_VALUE);
            post.setUpdateTime(LocalDateTime.now());
            if (create) {
                sysPostMapper.insert(post);
                log.info("创建租户默认岗位，tenantId={}，postId={}，postCode={}", tenantId, post.getId(), post.getPostCode());
            } else {
                sysPostMapper.updateById(post);
                log.info("复用租户默认岗位，tenantId={}，postId={}，postCode={}", tenantId, post.getId(), post.getPostCode());
            }
            return post;
        });
    }

    /**
     * 绑定管理员部门和岗位。
     *
     * @param tenantId 租户编号
     * @param adminUser 租户管理员用户
     * @param tenantDept 租户默认部门
     * @param tenantPost 租户默认岗位
     */
    private void bindAdminOrganization(String tenantId, SysUserEntity adminUser, SysDeptEntity tenantDept, SysPostEntity tenantPost) {
        ignoreTenantLine(() -> {
            adminUser.setDeptId(tenantDept.getId());
            adminUser.setUpdateTime(LocalDateTime.now());
            sysUserMapper.updateById(adminUser);
            SysUserPostEntity existing = sysUserPostMapper.selectOne(new LambdaQueryWrapper<SysUserPostEntity>()
                .eq(SysUserPostEntity::getTenantId, tenantId)
                .eq(SysUserPostEntity::getUserId, adminUser.getUserId())
                .eq(SysUserPostEntity::getPostId, tenantPost.getId())
                .last("limit 1"));
            if (existing == null) {
                SysUserPostEntity relation = new SysUserPostEntity();
                relation.setTenantId(tenantId);
                relation.setUserId(adminUser.getUserId());
                relation.setPostId(tenantPost.getId());
                relation.setCreateTime(LocalDateTime.now());
                relation.setUpdateTime(LocalDateTime.now());
                sysUserPostMapper.insert(relation);
                log.info("绑定租户管理员岗位，tenantId={}，userId={}，postId={}", tenantId, adminUser.getUserId(), tenantPost.getId());
            } else {
                log.info("复用租户管理员岗位绑定，tenantId={}，userId={}，postId={}", tenantId, adminUser.getUserId(), tenantPost.getId());
            }
            return null;
        });
    }

    /**
     * 加载平台默认部门模板。
     *
     * @return 平台默认部门模板
     */
    private SysDeptEntity loadPlatformDeptTemplate() {
        return sysDeptMapper.selectOne(new LambdaQueryWrapper<SysDeptEntity>()
            .eq(SysDeptEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID)
            .eq(SysDeptEntity::getParentId, SystemTenantConstants.ROOT_MENU_PARENT_ID)
            .eq(SysDeptEntity::getDeptName, SystemTenantConstants.DEFAULT_TENANT_DEPT_NAME)
            .last("limit 1"));
    }

    /**
     * 加载平台默认岗位模板。
     *
     * @return 平台默认岗位模板
     */
    private SysPostEntity loadPlatformPostTemplate() {
        return sysPostMapper.selectOne(new LambdaQueryWrapper<SysPostEntity>()
            .eq(SysPostEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID)
            .eq(SysPostEntity::getPostCode, SystemTenantConstants.DEFAULT_TENANT_ADMIN_POST_CODE)
            .last("limit 1"));
    }

    /**
     * 解析租户默认部门名称。
     *
     * @param template 平台部门模板
     * @return 部门名称
     */
    private String resolveDeptName(SysDeptEntity template) {
        return template == null ? SystemTenantConstants.DEFAULT_TENANT_DEPT_NAME : template.getDeptName();
    }

    /**
     * 解析租户默认部门祖级列表。
     *
     * @param template 平台部门模板
     * @return 祖级列表
     */
    private String resolveDeptAncestors(SysDeptEntity template) {
        return template == null ? SystemTenantConstants.DEFAULT_TENANT_DEPT_ANCESTORS : template.getAncestors();
    }

    /**
     * 解析租户默认部门排序。
     *
     * @param template 平台部门模板
     * @return 部门排序
     */
    private Integer resolveDeptOrder(SysDeptEntity template) {
        return template == null ? SystemTenantConstants.DEFAULT_TENANT_DEPT_ORDER : template.getOrderNum();
    }

    /**
     * 解析租户默认部门电话。
     *
     * @param tenant 租户实体
     * @param template 平台部门模板
     * @return 部门电话
     */
    private String resolveDeptPhone(SysTenantEntity tenant, SysDeptEntity template) {
        if (StringUtils.hasText(tenant.getContactPhone())) {
            return tenant.getContactPhone();
        }
        return template == null ? SystemTenantConstants.DEFAULT_TENANT_DEPT_PHONE : template.getPhone();
    }

    /**
     * 解析租户默认部门邮箱。
     *
     * @param template 平台部门模板
     * @return 部门邮箱
     */
    private String resolveDeptEmail(SysDeptEntity template) {
        return template == null ? SystemTenantConstants.DEFAULT_TENANT_DEPT_EMAIL : template.getEmail();
    }

    /**
     * 解析租户默认岗位编码。
     *
     * @param template 平台岗位模板
     * @return 岗位编码
     */
    private String resolvePostCode(SysPostEntity template) {
        return template == null ? SystemTenantConstants.DEFAULT_TENANT_ADMIN_POST_CODE : template.getPostCode();
    }

    /**
     * 解析租户默认岗位名称。
     *
     * @param template 平台岗位模板
     * @return 岗位名称
     */
    private String resolvePostName(SysPostEntity template) {
        return template == null ? SystemTenantConstants.DEFAULT_TENANT_ADMIN_POST_NAME : template.getPostName();
    }

    /**
     * 解析租户默认岗位排序。
     *
     * @param template 平台岗位模板
     * @return 岗位排序
     */
    private Integer resolvePostOrder(SysPostEntity template) {
        return template == null ? SystemTenantConstants.DEFAULT_TENANT_ADMIN_POST_ORDER : template.getOrderNum();
    }

    /**
     * 解析租户默认岗位备注。
     *
     * @param template 平台岗位模板
     * @return 岗位备注
     */
    private String resolvePostRemark(SysPostEntity template) {
        return template == null ? SystemTenantConstants.DEFAULT_TENANT_ADMIN_POST_REMARK : template.getRemark();
    }

    /**
     * 读取套餐绑定的平台模板菜单。
     *
     * @param packageId 套餐编号
     * @return 平台模板菜单列表
     */
    private List<SysMenuEntity> loadPackageTemplateMenus(Long packageId) {
        return ignoreTenantLine(() -> {
            List<Long> menuIds = sysTenantPackageMenuMapper.selectList(new LambdaQueryWrapper<SysTenantPackageMenuEntity>()
                    .eq(SysTenantPackageMenuEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID)
                    .eq(SysTenantPackageMenuEntity::getPackageId, packageId))
                .stream()
                .map(SysTenantPackageMenuEntity::getMenuId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
            List<SysMenuEntity> allPlatformMenus = loadAllPlatformMenus();
            List<Long> effectiveMenuIds = resolveEffectivePackageMenuIds(packageId, menuIds, allPlatformMenus);
            if (effectiveMenuIds.isEmpty()) {
                log.warn("租户套餐未绑定菜单，packageId={}", packageId);
                return List.of();
            }
            Set<Long> templateIds = new HashSet<>(appendMissingParentMenuIds(effectiveMenuIds));
            List<SysMenuEntity> templates = allPlatformMenus.stream()
                .filter(menu -> templateIds.contains(menu.getId()))
                .toList();
            if (templates.size() < effectiveMenuIds.size()) {
                log.warn("租户套餐存在无效菜单模板，packageId={}，packageMenuIds={}，validCount={}",
                    packageId, effectiveMenuIds, templates.size());
            }
            Map<Long, SysMenuEntity> templateMap = templates.stream()
                .collect(Collectors.toMap(SysMenuEntity::getId, Function.identity(), (left, right) -> left));
            return templates.stream()
                .sorted(Comparator.comparing((SysMenuEntity menu) -> menuDepth(menu, templateMap))
                    .thenComparing(SysMenuEntity::getOrderNum)
                    .thenComparing(SysMenuEntity::getId))
                .toList();
        });
    }

    /**
     * 加载平台租户全部菜单模板。
     *
     * @return 平台菜单模板列表
     */
    private List<SysMenuEntity> loadAllPlatformMenus() {
        return sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID));
    }

    /**
     * 解析套餐实际用于租户初始化的菜单编号。
     *
     * @param packageId 套餐编号
     * @param menuIds 套餐已绑定菜单编号
     * @param allPlatformMenus 平台菜单模板列表
     * @return 实际菜单编号列表
     */
    private List<Long> resolveEffectivePackageMenuIds(Long packageId, List<Long> menuIds, List<SysMenuEntity> allPlatformMenus) {
        if (isDefaultPackage(packageId) && menuIds.size() < allPlatformMenus.size()) {
            log.warn("默认套餐菜单绑定不完整，自动使用全部平台菜单初始化租户，packageId={}，bindCount={}，platformMenuCount={}",
                packageId, menuIds.size(), allPlatformMenus.size());
            return allPlatformMenus.stream()
                .map(SysMenuEntity::getId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        }
        return menuIds;
    }

    /**
     * 判断套餐是否为默认套餐。
     *
     * @param packageId 套餐编号
     * @return true 表示默认套餐
     */
    private boolean isDefaultPackage(Long packageId) {
        SysTenantPackageEntity packageEntity = sysTenantPackageMapper.selectOne(new LambdaQueryWrapper<SysTenantPackageEntity>()
            .eq(SysTenantPackageEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID)
            .eq(SysTenantPackageEntity::getId, packageId)
            .last("limit 1"));
        return packageEntity != null
            && (SystemDefaultDataGuard.isSystemDefault(packageEntity.getIsDefault())
                || "默认套餐".equals(packageEntity.getPackageName()));
    }

    /**
     * 复制套餐模板菜单到租户菜单表。
     *
     * @param tenantId 租户编号
     * @param templateMenus 平台模板菜单列表
     * @return 模板菜单编号到租户菜单的映射
     */
    private Map<Long, SysMenuEntity> copyPackageMenus(String tenantId, List<SysMenuEntity> templateMenus) {
        Map<Long, SysMenuEntity> copiedMenuMap = new HashMap<>();
        for (SysMenuEntity template : templateMenus) {
            SysMenuEntity copied = copySingleMenu(tenantId, template, copiedMenuMap);
            copiedMenuMap.put(template.getId(), copied);
        }
        return copiedMenuMap;
    }

    /**
     * 复制单个平台模板菜单。
     *
     * @param tenantId 租户编号
     * @param template 模板菜单
     * @param copiedMenuMap 已复制菜单映射
     * @return 租户菜单实体
     */
    private SysMenuEntity copySingleMenu(String tenantId, SysMenuEntity template, Map<Long, SysMenuEntity> copiedMenuMap) {
        return ignoreTenantLine(() -> {
            SysMenuEntity entity = sysMenuMapper.selectOne(new LambdaQueryWrapper<SysMenuEntity>()
                .eq(SysMenuEntity::getTenantId, tenantId)
                .eq(SysMenuEntity::getSourceMenuId, template.getId())
                .last("limit 1"));
            boolean create = entity == null;
            if (create) {
                entity = new SysMenuEntity();
                entity.setTenantId(tenantId);
                entity.setSourceMenuId(template.getId());
                entity.setCreateTime(LocalDateTime.now());
            }
            fillCopiedMenu(entity, template, copiedMenuMap);
            entity.setUpdateTime(LocalDateTime.now());
            if (create) {
                sysMenuMapper.insert(entity);
                log.info("复制租户菜单，tenantId={}，sourceMenuId={}，menuId={}", tenantId, template.getId(), entity.getId());
            } else {
                sysMenuMapper.updateById(entity);
                log.info("更新租户菜单，tenantId={}，sourceMenuId={}，menuId={}", tenantId, template.getId(), entity.getId());
            }
            return entity;
        });
    }

    /**
     * 填充复制后的租户菜单字段。
     *
     * @param entity 租户菜单实体
     * @param template 模板菜单实体
     * @param copiedMenuMap 已复制菜单映射
     */
    private void fillCopiedMenu(SysMenuEntity entity, SysMenuEntity template, Map<Long, SysMenuEntity> copiedMenuMap) {
        entity.setMenuName(template.getMenuName());
        entity.setParentId(resolveCopiedParentId(template, copiedMenuMap));
        entity.setOrderNum(template.getOrderNum());
        entity.setPath(template.getPath());
        entity.setComponent(template.getComponent());
        entity.setIsFrame(template.getIsFrame());
        entity.setMenuType(template.getMenuType());
        entity.setVisible(template.getVisible());
        entity.setStatus(template.getStatus());
        entity.setIsDefault(SystemTenantConstants.SYSTEM_DEFAULT_DATA_FLAG);
        entity.setPerms(template.getPerms());
        entity.setIcon(template.getIcon());
        entity.setRemark(template.getRemark());
    }

    /**
     * 解析复制后的父级菜单编号。
     *
     * @param template 模板菜单实体
     * @param copiedMenuMap 已复制菜单映射
     * @return 租户菜单父级编号
     */
    private Long resolveCopiedParentId(SysMenuEntity template, Map<Long, SysMenuEntity> copiedMenuMap) {
        Long parentId = DefaultValueUtils.defaultIfNull(template.getParentId(), SystemTenantConstants.ROOT_MENU_PARENT_ID);
        if (SystemTenantConstants.ROOT_MENU_PARENT_ID.equals(parentId)) {
            return SystemTenantConstants.ROOT_MENU_PARENT_ID;
        }
        SysMenuEntity copiedParent = copiedMenuMap.get(parentId);
        if (copiedParent == null) {
            log.warn("复制菜单时未找到父级模板映射，sourceMenuId={}，parentSourceMenuId={}", template.getId(), parentId);
            return SystemTenantConstants.ROOT_MENU_PARENT_ID;
        }
        return copiedParent.getId();
    }

    /**
     * 创建或更新默认角色。
     *
     * @param tenantId 租户编号
     * @param roleCode 角色编码
     * @param roleName 角色名称
     * @param orderNum 显示顺序
     * @return 角色实体
     */
    private SysRoleEntity upsertRole(String tenantId, String roleCode, String roleName, Integer orderNum) {
        return ignoreTenantLine(() -> {
            SysRoleEntity role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRoleEntity>()
                .eq(SysRoleEntity::getTenantId, tenantId)
                .eq(SysRoleEntity::getRoleCode, roleCode)
                .last("limit 1"));
            boolean create = role == null;
            if (create) {
                role = new SysRoleEntity();
                role.setTenantId(tenantId);
                role.setRoleCode(roleCode);
                role.setCreateTime(LocalDateTime.now());
            }
            role.setRoleName(roleName);
            role.setOrderNum(orderNum);
            role.setDataScope(SystemTenantConstants.DEFAULT_DATA_SCOPE);
            role.setStatus(SystemTenantConstants.STATUS_NORMAL_VALUE);
            role.setRemark("租户初始化默认角色");
            role.setUpdateTime(LocalDateTime.now());
            if (create) {
                sysRoleMapper.insert(role);
                log.info("创建租户默认角色，tenantId={}，roleCode={}，roleId={}", tenantId, roleCode, role.getId());
            } else {
                sysRoleMapper.updateById(role);
                log.info("复用租户默认角色，tenantId={}，roleCode={}，roleId={}", tenantId, roleCode, role.getId());
            }
            return role;
        });
    }

    /**
     * 覆盖绑定角色菜单。
     *
     * @param tenantId 租户编号
     * @param roleId 角色编号
     * @param menuIds 菜单编号列表
     */
    private void bindRoleMenus(String tenantId, Long roleId, List<Long> menuIds) {
        List<Long> distinctMenuIds = menuIds.stream().filter(Objects::nonNull).distinct().toList();
        log.info("初始化绑定角色菜单，tenantId={}，roleId={}，menuIds={}", tenantId, roleId, distinctMenuIds);
        ignoreTenantLine(() -> {
            sysRoleMenuMapper.physicalDeleteByRoleId(tenantId, roleId);
            for (Long menuId : distinctMenuIds) {
                SysRoleMenuEntity relation = new SysRoleMenuEntity();
                relation.setTenantId(tenantId);
                relation.setRoleId(roleId);
                relation.setMenuId(menuId);
                relation.setCreateTime(LocalDateTime.now());
                relation.setUpdateTime(LocalDateTime.now());
                sysRoleMenuMapper.insert(relation);
            }
            return null;
        });
    }

    /**
     * 覆盖绑定用户角色。
     *
     * @param tenantId 租户编号
     * @param userId 用户业务编号
     * @param roleId 角色编号
     */
    private void bindUserRole(String tenantId, String userId, Long roleId) {
        log.info("初始化绑定管理员角色，tenantId={}，userId={}，roleId={}", tenantId, userId, roleId);
        ignoreTenantLine(() -> {
            sysUserRoleMapper.physicalDeleteByUserId(tenantId, userId);
            SysUserRoleEntity relation = new SysUserRoleEntity();
            relation.setTenantId(tenantId);
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            relation.setCreateTime(LocalDateTime.now());
            relation.setUpdateTime(LocalDateTime.now());
            sysUserRoleMapper.insert(relation);
            return null;
        });
    }

    /**
     * 确保用户角色关系存在。
     *
     * @param tenantId 租户编号
     * @param userId 用户业务编号
     * @param roleId 角色编号
     */
    private void ensureUserRole(String tenantId, String userId, Long roleId) {
        log.info("确保管理员角色绑定存在，tenantId={}，userId={}，roleId={}", tenantId, userId, roleId);
        ignoreTenantLine(() -> {
            SysUserRoleEntity existing = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRoleEntity>()
                .eq(SysUserRoleEntity::getTenantId, tenantId)
                .eq(SysUserRoleEntity::getUserId, userId)
                .eq(SysUserRoleEntity::getRoleId, roleId)
                .last("limit 1"));
            if (existing == null) {
                SysUserRoleEntity relation = new SysUserRoleEntity();
                relation.setTenantId(tenantId);
                relation.setUserId(userId);
                relation.setRoleId(roleId);
                relation.setCreateTime(LocalDateTime.now());
                relation.setUpdateTime(LocalDateTime.now());
                sysUserRoleMapper.insert(relation);
                log.info("新增管理员角色绑定，tenantId={}，userId={}，roleId={}", tenantId, userId, roleId);
            }
            return null;
        });
    }

    /**
     * 解析普通用户基础菜单编号。
     *
     * @param templateMenus 平台模板菜单列表
     * @param copiedMenuMap 已复制菜单映射
     * @return 租户菜单编号列表
     */
    private List<Long> resolveTenantUserMenuIds(List<SysMenuEntity> templateMenus, Map<Long, SysMenuEntity> copiedMenuMap) {
        Map<Long, SysMenuEntity> templateMap = templateMenus.stream()
            .collect(Collectors.toMap(SysMenuEntity::getId, Function.identity(), (left, right) -> left));
        Set<Long> sourceMenuIds = new LinkedHashSet<>();
        for (SysMenuEntity template : templateMenus) {
            if (SystemTenantConstants.TENANT_USER_BASIC_PERMS.contains(template.getPerms())) {
                sourceMenuIds.add(template.getId());
                appendParentMenuId(template.getId(), templateMap, sourceMenuIds);
            }
        }
        return sourceMenuIds.stream()
            .map(copiedMenuMap::get)
            .filter(Objects::nonNull)
            .map(SysMenuEntity::getId)
            .toList();
    }

    /**
     * 补齐缺失的平台模板父菜单编号。
     *
     * @param menuIds 套餐菜单编号列表
     * @return 包含父菜单的模板菜单编号列表
     */
    private List<Long> appendMissingParentMenuIds(List<Long> menuIds) {
        List<SysMenuEntity> allPlatformMenus = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID));
        Map<Long, SysMenuEntity> menuMap = allPlatformMenus.stream()
            .collect(Collectors.toMap(SysMenuEntity::getId, Function.identity(), (left, right) -> left));
        Set<Long> result = new LinkedHashSet<>(menuIds);
        for (Long menuId : menuIds) {
            appendParentMenuId(menuId, menuMap, result);
        }
        return List.copyOf(result);
    }

    /**
     * 递归补齐父菜单编号。
     *
     * @param menuId 菜单编号
     * @param menuMap 菜单映射
     * @param result 结果集合
     */
    private void appendParentMenuId(Long menuId, Map<Long, SysMenuEntity> menuMap, Set<Long> result) {
        Set<Long> visited = new HashSet<>();
        Long currentMenuId = menuId;
        while (visited.add(currentMenuId)) {
            SysMenuEntity menu = menuMap.get(currentMenuId);
            if (menu == null || menu.getParentId() == null || SystemTenantConstants.ROOT_MENU_PARENT_ID.equals(menu.getParentId())) {
                return;
            }
            result.add(menu.getParentId());
            currentMenuId = menu.getParentId();
        }
    }

    /**
     * 计算菜单模板层级深度。
     *
     * @param menu 菜单实体
     * @param menuMap 菜单映射
     * @return 菜单深度
     */
    private int menuDepth(SysMenuEntity menu, Map<Long, SysMenuEntity> menuMap) {
        int depth = 0;
        Long parentId = menu.getParentId();
        Set<Long> visited = new HashSet<>();
        while (parentId != null && !SystemTenantConstants.ROOT_MENU_PARENT_ID.equals(parentId) && visited.add(parentId)) {
            depth++;
            SysMenuEntity parent = menuMap.get(parentId);
            parentId = parent == null ? SystemTenantConstants.ROOT_MENU_PARENT_ID : parent.getParentId();
        }
        return depth;
    }

    /**
     * 解析租户管理员昵称。
     *
     * @param tenant 租户实体
     * @return 管理员昵称
     */
    private String resolveAdminNickName(SysTenantEntity tenant) {
        if (StringUtils.hasText(tenant.getContactUserName())) {
            return tenant.getContactUserName();
        }
        return DefaultValueUtils.defaultIfBlank(tenant.getCompanyName(), "租户管理员");
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
