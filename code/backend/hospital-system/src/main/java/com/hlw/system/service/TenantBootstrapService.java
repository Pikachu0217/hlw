package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.util.DefaultValueUtils;
import com.hlw.common.security.PasswordEncoder;
import com.hlw.system.constants.SystemTenantConstants;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.entity.SysRoleEntity;
import com.hlw.system.entity.SysRoleMenuEntity;
import com.hlw.system.entity.SysTenantEntity;
import com.hlw.system.entity.SysTenantPackageMenuEntity;
import com.hlw.system.entity.SysUserEntity;
import com.hlw.system.entity.SysUserRoleEntity;
import com.hlw.system.mapper.SysMenuMapper;
import com.hlw.system.mapper.SysRoleMapper;
import com.hlw.system.mapper.SysRoleMenuMapper;
import com.hlw.system.mapper.SysTenantPackageMenuMapper;
import com.hlw.system.mapper.SysUserMapper;
import com.hlw.system.mapper.SysUserRoleMapper;
import com.hlw.system.service.support.MybatisTenantHelpers;
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
    /** 租户套餐菜单关系数据访问组件。 */
    private final SysTenantPackageMenuMapper sysTenantPackageMenuMapper;
    /** 角色数据访问组件。 */
    private final SysRoleMapper sysRoleMapper;
    /** 角色菜单关系数据访问组件。 */
    private final SysRoleMenuMapper sysRoleMenuMapper;
    /** 用户数据访问组件。 */
    private final SysUserMapper sysUserMapper;
    /** 用户角色关系数据访问组件。 */
    private final SysUserRoleMapper sysUserRoleMapper;

    /**
     * 初始化新租户的菜单、默认角色、管理员账号和授权关系。
     *
     * @param tenant 租户实体
     */
    public void initializeTenant(SysTenantEntity tenant) {
        if (tenant.getPackageId() == null) {
            log.warn("租户初始化失败，租户未绑定套餐，tenantId={}", tenant.getTenantId());
            throw new BizException(400, "租户套餐不能为空");
        }
        log.info("开始初始化租户权限数据，tenantId={}，packageId={}", tenant.getTenantId(), tenant.getPackageId());
        SysUserEntity adminUser = upsertTenantAdminUser(tenant);
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
        bindUserRole(tenant.getTenantId(), adminUser.getUserId(), adminRole.getId());
        log.info("租户权限数据初始化完成，tenantId={}，menuCount={}，adminRoleId={}，userRoleId={}，adminUserId={}",
            tenant.getTenantId(), tenantMenuMap.size(), adminRole.getId(), userRole.getId(), adminUser.getUserId());
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
            if (menuIds.isEmpty()) {
                log.warn("租户套餐未绑定菜单，packageId={}", packageId);
                return List.of();
            }
            List<SysMenuEntity> templates = sysMenuMapper.selectList(new LambdaQueryWrapper<SysMenuEntity>()
                .eq(SysMenuEntity::getTenantId, SystemTenantConstants.PLATFORM_TENANT_ID)
                .in(SysMenuEntity::getId, appendMissingParentMenuIds(menuIds)));
            if (templates.size() < menuIds.size()) {
                log.warn("租户套餐存在无效菜单模板，packageId={}，packageMenuIds={}，validCount={}",
                    packageId, menuIds, templates.size());
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
