package com.hlw.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.plugins.IgnoreStrategy;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.hlw.common.core.exception.BizException;
import com.hlw.common.core.tenant.TenantContext;
import com.hlw.system.entity.SysConfigEntity;
import com.hlw.system.entity.SysDictEntity;
import com.hlw.system.entity.SysMenuEntity;
import com.hlw.system.entity.SysPermissionEntity;
import com.hlw.system.entity.SysPostEntity;
import com.hlw.system.entity.SysRoleEntity;
import com.hlw.system.entity.SysRoleMenuEntity;
import com.hlw.system.entity.SysTenantEntity;
import com.hlw.system.entity.SysUserEntity;
import com.hlw.system.entity.SysUserPostEntity;
import com.hlw.system.entity.SysUserRoleEntity;
import com.hlw.system.dto.BindRoleMenuRequest;
import com.hlw.system.dto.BindUserRoleRequest;
import com.hlw.system.dto.CreateDictRequest;
import com.hlw.system.dto.CreateMenuRequest;
import com.hlw.system.dto.CreatePermissionRequest;
import com.hlw.system.dto.CreatePostRequest;
import com.hlw.system.dto.CreateRoleRequest;
import com.hlw.system.dto.CreateTenantRequest;
import com.hlw.system.dto.CreateUserRequest;
import com.hlw.system.dto.UpdateConfigRequest;
import com.hlw.system.mapper.SysConfigMapper;
import com.hlw.system.mapper.SysDictMapper;
import com.hlw.system.mapper.SysMenuMapper;
import com.hlw.system.mapper.SysPermissionMapper;
import com.hlw.system.mapper.SysPostMapper;
import com.hlw.system.mapper.SysRoleMapper;
import com.hlw.system.mapper.SysRoleMenuMapper;
import com.hlw.system.mapper.SysTenantMapper;
import com.hlw.system.mapper.SysUserMapper;
import com.hlw.system.mapper.SysUserPostMapper;
import com.hlw.system.mapper.SysUserRoleMapper;
import com.hlw.system.vo.ConfigVO;
import com.hlw.system.vo.DictVO;
import com.hlw.system.vo.MenuVO;
import com.hlw.system.vo.PermissionVO;
import com.hlw.system.vo.PostVO;
import com.hlw.system.vo.RelationBindingVO;
import com.hlw.system.vo.RoleMenuVO;
import com.hlw.system.vo.RoleVO;
import com.hlw.system.vo.TenantVO;
import com.hlw.system.vo.UserRoleVO;
import com.hlw.system.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 系统管理服务，负责租户、用户、角色、菜单和授权等业务编排。
 */
@Service
@RequiredArgsConstructor
public class SystemTenantContextService {
    private static final Logger log = LoggerFactory.getLogger(SystemTenantContextService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String DEFAULT_STATUS = "启用";
    private static final String DEFAULT_TENANT_STATUS = "正常";
    private static final String DEFAULT_DATA_SCOPE = "本租户数据";
    private static final String DEFAULT_MENU_TYPE = "菜单";
    private static final String DEFAULT_RESOURCE_TYPE = "按钮";
    private static final Long DEFAULT_RELATION_ID = 0L;

    /** 租户数据访问组件。 */
    private final SysTenantMapper sysTenantMapper;
    /** 用户数据访问组件。 */
    private final SysUserMapper sysUserMapper;
    /** 角色数据访问组件。 */
    private final SysRoleMapper sysRoleMapper;
    /** 菜单数据访问组件。 */
    private final SysMenuMapper sysMenuMapper;
    /** 字典数据访问组件。 */
    private final SysDictMapper sysDictMapper;
    /** 参数配置数据访问组件。 */
    private final SysConfigMapper sysConfigMapper;
    /** 岗位数据访问组件。 */
    private final SysPostMapper sysPostMapper;
    /** 权限码数据访问组件。 */
    private final SysPermissionMapper sysPermissionMapper;
    /** 用户角色数据访问组件。 */
    private final SysUserRoleMapper sysUserRoleMapper;
    /** 角色菜单数据访问组件。 */
    private final SysRoleMenuMapper sysRoleMenuMapper;
    /** 用户岗位数据访问组件。 */
    private final SysUserPostMapper sysUserPostMapper;

    /**
     * 查询租户列表。
     *
     * @return 租户展示列表
     */
    public List<TenantVO> listTenants() {
        log.info("查询租户列表");
        Long currentTenantId = TenantContext.getTenantId();
        List<SysTenantEntity> tenantEntities = TenantContext.isPlatformRequest()
            ? InterceptorIgnoreHelper.execute(ignoreTenantLine(), () -> sysTenantMapper.selectList(platformTenantWrapper()))
            : currentTenantId == null || currentTenantId <= 0L
                ? List.of()
                : sysTenantMapper.selectList(new LambdaQueryWrapper<SysTenantEntity>()
                    .eq(SysTenantEntity::getDeleted, 0)
                    .eq(SysTenantEntity::getTenantId, currentTenantId));
        return tenantEntities
            .stream()
            .sorted(Comparator.comparing(SysTenantEntity::getId))
            .map(this::toTenantVO)
            .toList();
    }

    /**
     * 创建租户。
     *
     * @param request 创建租户请求
     * @return 新建租户展示对象
     */
    @Transactional
    public TenantVO createTenant(CreateTenantRequest request) {
        ensurePlatformContext("只有平台上下文允许创建租户");
        log.info("创建租户，tenantName={}，packageName={}，adminName={}",
            request.getTenantName(), request.getPackageName(), request.getAdminName());
        SysTenantEntity entity = new SysTenantEntity();
        entity.setTenantId(nextTenantId());
        entity.setName(request.getTenantName());
        entity.setTenantName(request.getTenantName());
        entity.setPackageName(request.getPackageName());
        entity.setAdminName(request.getAdminName());
        entity.setExpireAt(parseDate(request.getExpireAt()));
        entity.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_TENANT_STATUS));
        entity.setDeleted(0);
        assertTenantIdNotExists(entity.getTenantId());
        InterceptorIgnoreHelper.execute(ignoreTenantLine(), () -> sysTenantMapper.insert(entity));
        return toTenantVO(entity);
    }

    /**
     * 查询用户列表。
     *
     * @return 用户展示列表
     */
    public List<UserVO> listUsers() {
        log.info("查询系统用户列表");
        List<SysUserEntity> users = sysUserMapper.selectList(activeUserWrapper());
        Map<Long, SysPostEntity> postMap = sysPostMapper.selectList(activePostWrapper())
            .stream()
            .collect(Collectors.toMap(SysPostEntity::getId, post -> post));
        Map<Long, List<SysUserPostEntity>> userPostMap = sysUserPostMapper.selectList(activeUserPostWrapper())
            .stream()
            .collect(Collectors.groupingBy(SysUserPostEntity::getUserId));
        List<UserVO> result = new ArrayList<>();
        for (SysUserEntity user : users.stream().sorted(Comparator.comparing(SysUserEntity::getId)).toList()) {
            UserVO vo = new UserVO();
            vo.setKey(String.valueOf(user.getId()));
            vo.setUsername(user.getUsername());
            vo.setDeptName(user.getDeptName());
            vo.setRoleName(user.getRoleName());
            vo.setPhone(user.getPhone());
            vo.setLastLogin(user.getLastLogin());
            vo.setStatus(user.getStatus());
            vo.setPostName(resolvePostName(user.getId(), userPostMap, postMap));
            result.add(vo);
        }
        return result;
    }

    /**
     * 创建用户。
     *
     * @param request 创建用户请求
     * @return 新建用户展示对象
     */
    @Transactional
    public UserVO createUser(CreateUserRequest request) {
        log.info("创建后台用户，username={}，deptName={}，roleName={}",
            request.getUsername(), request.getDeptName(), request.getRoleName());
        SysUserEntity entity = new SysUserEntity();
        entity.setUsername(request.getUsername());
        entity.setPassword(defaultIfBlank(request.getPassword(), "{noop}123456"));
        entity.setPhone(defaultIfBlank(request.getPhone(), ""));
        entity.setUserType(defaultIfBlank(request.getUserType(), "ADMIN"));
        entity.setDeptName(defaultIfBlank(request.getDeptName(), "运营部"));
        entity.setRoleName(defaultIfBlank(request.getRoleName(), "系统管理员"));
        entity.setLastLogin("-");
        entity.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_STATUS));
        entity.setDeleted(0);
        sysUserMapper.insert(entity);
        UserVO vo = new UserVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setUsername(entity.getUsername());
        vo.setDeptName(entity.getDeptName());
        vo.setRoleName(entity.getRoleName());
        vo.setPostName("-");
        vo.setPhone(entity.getPhone());
        vo.setLastLogin(entity.getLastLogin());
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * 查询角色列表。
     *
     * @return 角色展示列表
     */
    public List<RoleVO> listRoles() {
        log.info("查询系统角色列表");
        Map<Long, Integer> memberCountMap = new LinkedHashMap<>();
        for (SysUserRoleEntity relation : sysUserRoleMapper.selectList(activeUserRoleWrapper())) {
            memberCountMap.merge(relation.getRoleId(), 1, Integer::sum);
        }
        return sysRoleMapper.selectList(activeRoleWrapper())
            .stream()
            .sorted(Comparator.comparing(SysRoleEntity::getId))
            .map(role -> toRoleVO(role, memberCountMap.getOrDefault(role.getId(), 0)))
            .toList();
    }

    /**
     * 创建角色。
     *
     * @param request 创建角色请求
     * @return 新建角色展示对象
     */
    @Transactional
    public RoleVO createRole(CreateRoleRequest request) {
        log.info("创建角色，roleName={}，roleCode={}", request.getRoleName(), request.getRoleCode());
        SysRoleEntity entity = new SysRoleEntity();
        entity.setRoleName(request.getRoleName());
        entity.setRoleCode(request.getRoleCode());
        entity.setDataScope(defaultIfBlank(request.getDataScope(), DEFAULT_DATA_SCOPE));
        entity.setMemberCount(0);
        entity.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_STATUS));
        entity.setDeleted(0);
        sysRoleMapper.insert(entity);
        return toRoleVO(entity, 0);
    }

    /**
     * 查询菜单列表。
     *
     * @return 菜单展示列表
     */
    public List<MenuVO> listMenus() {
        log.info("查询系统菜单列表");
        return sysMenuMapper.selectList(activeMenuWrapper())
            .stream()
            .sorted(Comparator.comparing(SysMenuEntity::getParentId).thenComparing(SysMenuEntity::getSort).thenComparing(SysMenuEntity::getId))
            .map(this::toMenuVO)
            .toList();
    }

    /**
     * 创建菜单。
     *
     * @param request 创建菜单请求
     * @return 新建菜单展示对象
     */
    @Transactional
    public MenuVO createMenu(CreateMenuRequest request) {
        log.info("创建菜单，menuName={}，permission={}，routePath={}",
            request.getMenuName(), request.getPermission(), request.getRoutePath());
        SysMenuEntity entity = new SysMenuEntity();
        entity.setMenuName(request.getMenuName());
        entity.setPermission(request.getPermission());
        entity.setRoutePath(request.getRoutePath());
        entity.setMenuType(defaultIfBlank(request.getMenuType(), DEFAULT_MENU_TYPE));
        entity.setParentId(defaultLong(request.getParentId(), 0L));
        entity.setSort(defaultInt(request.getSort(), 0));
        entity.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_STATUS));
        entity.setDeleted(0);
        sysMenuMapper.insert(entity);
        return toMenuVO(entity);
    }

    /**
     * 查询字典列表。
     *
     * @return 字典展示列表
     */
    public List<DictVO> listDicts() {
        log.info("查询系统字典列表");
        return sysDictMapper.selectList(activeDictWrapper())
            .stream()
            .sorted(Comparator.comparing(SysDictEntity::getDictType).thenComparing(SysDictEntity::getSort).thenComparing(SysDictEntity::getId))
            .map(this::toDictVO)
            .toList();
    }

    /**
     * 创建字典项。
     *
     * @param request 创建字典请求
     * @return 新建字典展示对象
     */
    @Transactional
    public DictVO createDict(CreateDictRequest request) {
        log.info("创建字典项，dictType={}，dictLabel={}", request.getDictType(), request.getDictLabel());
        SysDictEntity entity = new SysDictEntity();
        entity.setDictType(request.getDictType());
        entity.setDictLabel(request.getDictLabel());
        entity.setDictValue(request.getDictValue());
        entity.setSort(defaultInt(request.getSort(), 0));
        entity.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_STATUS));
        entity.setRemark(defaultIfBlank(request.getRemark(), ""));
        entity.setDeleted(0);
        sysDictMapper.insert(entity);
        return toDictVO(entity);
    }

    /**
     * 查询参数配置列表。
     *
     * @return 参数配置展示列表
     */
    public List<ConfigVO> listConfigs() {
        log.info("查询系统参数配置列表");
        return sysConfigMapper.selectList(activeConfigWrapper())
            .stream()
            .sorted(Comparator.comparing(SysConfigEntity::getId))
            .map(this::toConfigVO)
            .toList();
    }

    /**
     * 更新参数配置。
     *
     * @param id 配置编号
     * @param request 更新参数请求
     * @return 更新后的参数展示对象
     */
    @Transactional
    public ConfigVO updateConfig(Long id, UpdateConfigRequest request) {
        log.info("更新系统配置，configId={}", id);
        SysConfigEntity entity = requireActiveConfig(id);
        entity.setConfigValue(request.getConfigValue());
        entity.setRemark(defaultIfBlank(request.getRemark(), ""));
        sysConfigMapper.updateById(entity);
        return toConfigVO(entity);
    }

    /**
     * 查询岗位列表。
     *
     * @return 岗位展示列表
     */
    public List<PostVO> listPosts() {
        log.info("查询系统岗位列表");
        return sysPostMapper.selectList(activePostWrapper())
            .stream()
            .sorted(Comparator.comparing(SysPostEntity::getSort).thenComparing(SysPostEntity::getId))
            .map(this::toPostVO)
            .toList();
    }

    /**
     * 创建岗位。
     *
     * @param request 创建岗位请求
     * @return 新建岗位展示对象
     */
    @Transactional
    public PostVO createPost(CreatePostRequest request) {
        log.info("创建岗位，postName={}，postCode={}", request.getPostName(), request.getPostCode());
        SysPostEntity entity = new SysPostEntity();
        entity.setPostName(request.getPostName());
        entity.setPostCode(request.getPostCode());
        entity.setSort(defaultInt(request.getSort(), 0));
        entity.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_STATUS));
        entity.setRemark(defaultIfBlank(request.getRemark(), ""));
        entity.setDeleted(0);
        sysPostMapper.insert(entity);
        return toPostVO(entity);
    }

    /**
     * 查询权限码列表。
     *
     * @return 权限码展示列表
     */
    public List<PermissionVO> listPermissions() {
        log.info("查询系统权限码列表");
        Map<Long, String> menuMap = sysMenuMapper.selectList(activeMenuWrapper())
            .stream()
            .collect(Collectors.toMap(SysMenuEntity::getId, SysMenuEntity::getMenuName));
        return sysPermissionMapper.selectList(activePermissionWrapper())
            .stream()
            .sorted(Comparator.comparing(SysPermissionEntity::getId))
            .map(permission -> toPermissionVO(permission, menuMap.get(permission.getMenuId())))
            .toList();
    }

    /**
     * 创建权限码。
     *
     * @param request 创建权限码请求
     * @return 新建权限码展示对象
     */
    @Transactional
    public PermissionVO createPermission(CreatePermissionRequest request) {
        log.info("创建权限码，permissionName={}，permissionCode={}，resourceType={}",
            request.getPermissionName(), request.getPermissionCode(), request.getResourceType());
        String menuName = "-";
        if (request.getMenuId() != null) {
            menuName = requireActiveMenu(request.getMenuId()).getMenuName();
        }
        SysPermissionEntity entity = new SysPermissionEntity();
        entity.setPermissionName(request.getPermissionName());
        entity.setPermissionCode(request.getPermissionCode());
        entity.setResourceType(defaultIfBlank(request.getResourceType(), DEFAULT_RESOURCE_TYPE));
        entity.setMenuId(request.getMenuId());
        entity.setStatus(defaultIfBlank(request.getStatus(), DEFAULT_STATUS));
        entity.setDeleted(0);
        sysPermissionMapper.insert(entity);
        return toPermissionVO(entity, menuName);
    }

    /**
     * 查询用户角色授权列表。
     *
     * @return 用户角色授权展示列表
     */
    public List<UserRoleVO> listUserRoles() {
        log.info("查询用户角色授权列表");
        Map<Long, String> userMap = sysUserMapper.selectList(activeUserWrapper())
            .stream()
            .collect(Collectors.toMap(SysUserEntity::getId, SysUserEntity::getUsername));
        Map<Long, String> roleMap = sysRoleMapper.selectList(activeRoleWrapper())
            .stream()
            .collect(Collectors.toMap(SysRoleEntity::getId, SysRoleEntity::getRoleName));
        return sysUserRoleMapper.selectList(activeUserRoleWrapper())
            .stream()
            .sorted(Comparator.comparing(SysUserRoleEntity::getId))
            .map(relation -> {
                UserRoleVO vo = new UserRoleVO();
                vo.setKey(String.valueOf(relation.getId()));
                vo.setUsername(userMap.getOrDefault(relation.getUserId(), "-"));
                vo.setRoleName(roleMap.getOrDefault(relation.getRoleId(), "-"));
                vo.setStatus(relation.getStatus());
                return vo;
            })
            .toList();
    }

    /**
     * 绑定用户角色。
     *
     * @param request 绑定用户角色请求
     * @return 绑定展示对象
     */
    @Transactional
    public RelationBindingVO bindUserRole(BindUserRoleRequest request) {
        log.info("绑定用户角色，userId={}，roleId={}", request.getUserId(), request.getRoleId());
        requireActiveUser(request.getUserId());
        requireActiveRole(request.getRoleId());
        SysUserRoleEntity existed = sysUserRoleMapper.selectOne(new LambdaQueryWrapper<SysUserRoleEntity>()
            .eq(SysUserRoleEntity::getDeleted, 0)
            .eq(SysUserRoleEntity::getUserId, request.getUserId())
            .eq(SysUserRoleEntity::getRoleId, request.getRoleId())
            .last("limit 1"));
        if (existed != null) {
            log.info("用户角色已绑定，userId={}，roleId={}", request.getUserId(), request.getRoleId());
            return toRelationBindingVO(existed, request.getUserId(), request.getRoleId(), null);
        }
        SysUserRoleEntity entity = new SysUserRoleEntity();
        entity.setUserId(request.getUserId());
        entity.setRoleId(request.getRoleId());
        entity.setStatus(DEFAULT_STATUS);
        entity.setDeleted(0);
        sysUserRoleMapper.insert(entity);
        refreshRoleMemberCount(request.getRoleId());
        return toRelationBindingVO(entity, request.getUserId(), request.getRoleId(), null);
    }

    /**
     * 查询角色菜单授权列表。
     *
     * @return 角色菜单授权展示列表
     */
    public List<RoleMenuVO> listRoleMenus() {
        log.info("查询角色菜单授权列表");
        Map<Long, String> roleMap = sysRoleMapper.selectList(activeRoleWrapper())
            .stream()
            .collect(Collectors.toMap(SysRoleEntity::getId, SysRoleEntity::getRoleName));
        Map<Long, SysMenuEntity> menuMap = sysMenuMapper.selectList(activeMenuWrapper())
            .stream()
            .collect(Collectors.toMap(SysMenuEntity::getId, menu -> menu));
        return sysRoleMenuMapper.selectList(activeRoleMenuWrapper())
            .stream()
            .sorted(Comparator.comparing(SysRoleMenuEntity::getId))
            .map(relation -> {
                RoleMenuVO vo = new RoleMenuVO();
                vo.setKey(String.valueOf(relation.getId()));
                vo.setRoleName(roleMap.getOrDefault(relation.getRoleId(), "-"));
                SysMenuEntity menu = menuMap.get(relation.getMenuId());
                vo.setMenuName(menu == null ? "-" : menu.getMenuName());
                vo.setPermission(menu == null ? "-" : menu.getPermission());
                vo.setStatus(relation.getStatus());
                return vo;
            })
            .toList();
    }

    /**
     * 绑定角色菜单。
     *
     * @param request 绑定角色菜单请求
     * @return 绑定展示对象
     */
    @Transactional
    public RelationBindingVO bindRoleMenu(BindRoleMenuRequest request) {
        log.info("绑定角色菜单，roleId={}，menuId={}", request.getRoleId(), request.getMenuId());
        requireActiveRole(request.getRoleId());
        requireActiveMenu(request.getMenuId());
        SysRoleMenuEntity existed = sysRoleMenuMapper.selectOne(new LambdaQueryWrapper<SysRoleMenuEntity>()
            .eq(SysRoleMenuEntity::getDeleted, 0)
            .eq(SysRoleMenuEntity::getRoleId, request.getRoleId())
            .eq(SysRoleMenuEntity::getMenuId, request.getMenuId())
            .last("limit 1"));
        if (existed != null) {
            log.info("角色菜单已绑定，roleId={}，menuId={}", request.getRoleId(), request.getMenuId());
            return toRelationBindingVO(existed, null, request.getRoleId(), request.getMenuId());
        }
        SysRoleMenuEntity entity = new SysRoleMenuEntity();
        entity.setRoleId(request.getRoleId());
        entity.setMenuId(request.getMenuId());
        entity.setStatus(DEFAULT_STATUS);
        entity.setDeleted(0);
        sysRoleMenuMapper.insert(entity);
        return toRelationBindingVO(entity, null, request.getRoleId(), request.getMenuId());
    }

    /**
     * 构造租户激活数据查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysTenantEntity> activeTenantWrapper() {
        return new LambdaQueryWrapper<SysTenantEntity>().eq(SysTenantEntity::getDeleted, 0);
    }

    /**
     * 构造平台级租户查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysTenantEntity> platformTenantWrapper() {
        return new LambdaQueryWrapper<SysTenantEntity>().eq(SysTenantEntity::getDeleted, 0);
    }

    /**
     * 构造忽略租户行拦截策略。
     *
     * @return 忽略策略
     */
    private IgnoreStrategy ignoreTenantLine() {
        return IgnoreStrategy.builder().tenantLine(true).build();
    }

    /**
     * 构造用户激活数据查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysUserEntity> activeUserWrapper() {
        return new LambdaQueryWrapper<SysUserEntity>().eq(SysUserEntity::getDeleted, 0);
    }

    /**
     * 构造角色激活数据查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysRoleEntity> activeRoleWrapper() {
        return new LambdaQueryWrapper<SysRoleEntity>().eq(SysRoleEntity::getDeleted, 0);
    }

    /**
     * 构造菜单激活数据查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysMenuEntity> activeMenuWrapper() {
        return new LambdaQueryWrapper<SysMenuEntity>().eq(SysMenuEntity::getDeleted, 0);
    }

    /**
     * 构造字典激活数据查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysDictEntity> activeDictWrapper() {
        return new LambdaQueryWrapper<SysDictEntity>().eq(SysDictEntity::getDeleted, 0);
    }

    /**
     * 构造配置激活数据查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysConfigEntity> activeConfigWrapper() {
        return new LambdaQueryWrapper<SysConfigEntity>().eq(SysConfigEntity::getDeleted, 0);
    }

    /**
     * 构造岗位激活数据查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysPostEntity> activePostWrapper() {
        return new LambdaQueryWrapper<SysPostEntity>().eq(SysPostEntity::getDeleted, 0);
    }

    /**
     * 构造权限激活数据查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysPermissionEntity> activePermissionWrapper() {
        return new LambdaQueryWrapper<SysPermissionEntity>().eq(SysPermissionEntity::getDeleted, 0);
    }

    /**
     * 构造用户角色激活数据查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysUserRoleEntity> activeUserRoleWrapper() {
        return new LambdaQueryWrapper<SysUserRoleEntity>().eq(SysUserRoleEntity::getDeleted, 0);
    }

    /**
     * 构造角色菜单激活数据查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysRoleMenuEntity> activeRoleMenuWrapper() {
        return new LambdaQueryWrapper<SysRoleMenuEntity>().eq(SysRoleMenuEntity::getDeleted, 0);
    }

    /**
     * 构造用户岗位激活数据查询条件。
     *
     * @return 查询条件
     */
    private LambdaQueryWrapper<SysUserPostEntity> activeUserPostWrapper() {
        return new LambdaQueryWrapper<SysUserPostEntity>().eq(SysUserPostEntity::getDeleted, 0);
    }

    /**
     * 刷新角色成员数量。
     *
     * @param roleId 角色编号
     */
    private void refreshRoleMemberCount(Long roleId) {
        SysRoleEntity role = requireActiveRole(roleId);
        int count = (int) sysUserRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRoleEntity>()
            .eq(SysUserRoleEntity::getDeleted, 0)
            .eq(SysUserRoleEntity::getRoleId, roleId));
        role.setMemberCount(count);
        sysRoleMapper.updateById(role);
    }

    /**
     * 生成下一个租户编号。
     *
     * @return 租户编号
     */
    private Long nextTenantId() {
        return InterceptorIgnoreHelper.execute(ignoreTenantLine(), () -> sysTenantMapper.selectList(platformTenantWrapper()))
            .stream()
            .map(SysTenantEntity::getTenantId)
            .filter(Objects::nonNull)
            .max(Long::compareTo)
            .map(value -> value + 100L)
            .orElse(100L);
    }

    /**
     * 解析日期字符串。
     *
     * @param value 日期字符串
     * @return 日期对象
     */
    private LocalDate parseDate(String value) {
        try {
            return LocalDate.parse(value, DATE_FORMATTER);
        } catch (Exception exception) {
            throw new BizException(400, "到期日期格式不正确");
        }
    }

    /**
     * 校验实体是否存在。
     *
     * @param entity 查询结果
     * @param message 错误消息
     * @param <T> 实体类型
     * @return 非空实体
     */
    private <T> T requireEntity(T entity, String message) {
        if (entity == null) {
            throw new BizException(404, message);
        }
        return entity;
    }

    /**
     * 校验当前请求是否处于平台上下文。
     *
     * @param message 不满足条件时的错误消息
     */
    private void ensurePlatformContext(String message) {
        if (!TenantContext.isPlatformRequest()) {
            throw new BizException(403, message);
        }
    }

    /**
     * 校验租户编号未被占用。
     *
     * @param tenantId 租户编号
     */
    private void assertTenantIdNotExists(Long tenantId) {
        Long count = InterceptorIgnoreHelper.execute(
            ignoreTenantLine(),
            () -> sysTenantMapper.selectCount(new LambdaQueryWrapper<SysTenantEntity>()
                .eq(SysTenantEntity::getDeleted, 0)
                .eq(SysTenantEntity::getTenantId, tenantId))
        );
        if (count != null && count > 0L) {
            throw new BizException(409, "租户编号已存在，请重试");
        }
    }

    /**
     * 校验用户处于可用状态。
     *
     * @param userId 用户编号
     * @return 用户实体
     */
    private SysUserEntity requireActiveUser(Long userId) {
        return requireEntity(sysUserMapper.selectOne(new LambdaQueryWrapper<SysUserEntity>()
            .eq(SysUserEntity::getDeleted, 0)
            .eq(SysUserEntity::getId, userId)
            .last("limit 1")), "用户不存在");
    }

    /**
     * 校验角色处于可用状态。
     *
     * @param roleId 角色编号
     * @return 角色实体
     */
    private SysRoleEntity requireActiveRole(Long roleId) {
        return requireEntity(sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRoleEntity>()
            .eq(SysRoleEntity::getDeleted, 0)
            .eq(SysRoleEntity::getId, roleId)
            .last("limit 1")), "角色不存在");
    }

    /**
     * 校验菜单处于可用状态。
     *
     * @param menuId 菜单编号
     * @return 菜单实体
     */
    private SysMenuEntity requireActiveMenu(Long menuId) {
        return requireEntity(sysMenuMapper.selectOne(new LambdaQueryWrapper<SysMenuEntity>()
            .eq(SysMenuEntity::getDeleted, 0)
            .eq(SysMenuEntity::getId, menuId)
            .last("limit 1")), "菜单不存在");
    }

    /**
     * 校验参数配置处于可用状态。
     *
     * @param configId 配置编号
     * @return 配置实体
     */
    private SysConfigEntity requireActiveConfig(Long configId) {
        return requireEntity(sysConfigMapper.selectOne(new LambdaQueryWrapper<SysConfigEntity>()
            .eq(SysConfigEntity::getDeleted, 0)
            .eq(SysConfigEntity::getId, configId)
            .last("limit 1")), "系统配置不存在");
    }

    /**
     * 读取默认字符串。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后字符串
     */
    private String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    /**
     * 读取默认整型。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后整型
     */
    private Integer defaultInt(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * 读取默认长整型。
     *
     * @param value 原始值
     * @param defaultValue 默认值
     * @return 处理后长整型
     */
    private Long defaultLong(Long value, Long defaultValue) {
        return value == null ? defaultValue : value;
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
     * 转换为租户展示对象。
     *
     * @param entity 租户实体
     * @return 租户展示对象
     */
    private TenantVO toTenantVO(SysTenantEntity entity) {
        TenantVO vo = new TenantVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setTenantId(entity.getTenantId());
        vo.setTenantName(entity.getTenantName());
        vo.setPackageName(entity.getPackageName());
        vo.setAdminName(entity.getAdminName());
        vo.setExpireAt(entity.getExpireAt() == null ? "" : entity.getExpireAt().format(DATE_FORMATTER));
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * 转换为角色展示对象。
     *
     * @param entity 角色实体
     * @param memberCount 成员数量
     * @return 角色展示对象
     */
    private RoleVO toRoleVO(SysRoleEntity entity, Integer memberCount) {
        RoleVO vo = new RoleVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setRoleName(entity.getRoleName());
        vo.setRoleCode(entity.getRoleCode());
        vo.setDataScope(entity.getDataScope());
        vo.setMemberCount(memberCount);
        vo.setUpdatedAt(entity.getUpdateTime() == null ? "-" : entity.getUpdateTime().format(DATE_TIME_FORMATTER));
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * 转换为菜单展示对象。
     *
     * @param entity 菜单实体
     * @return 菜单展示对象
     */
    private MenuVO toMenuVO(SysMenuEntity entity) {
        MenuVO vo = new MenuVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setParentId(String.valueOf(defaultLong(entity.getParentId(), 0L)));
        vo.setMenuName(entity.getMenuName());
        vo.setMenuType(entity.getMenuType());
        vo.setPermission(entity.getPermission());
        vo.setRoutePath(entity.getRoutePath());
        vo.setSort(defaultInt(entity.getSort(), 0));
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * 转换为字典展示对象。
     *
     * @param entity 字典实体
     * @return 字典展示对象
     */
    private DictVO toDictVO(SysDictEntity entity) {
        DictVO vo = new DictVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setDictType(entity.getDictType());
        vo.setDictLabel(entity.getDictLabel());
        vo.setDictValue(entity.getDictValue());
        vo.setSort(defaultInt(entity.getSort(), 0));
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    /**
     * 转换为配置展示对象。
     *
     * @param entity 配置实体
     * @return 配置展示对象
     */
    private ConfigVO toConfigVO(SysConfigEntity entity) {
        ConfigVO vo = new ConfigVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setConfigKey(entity.getConfigKey());
        vo.setConfigValue(entity.getConfigValue());
        vo.setConfigType(entity.getConfigType());
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    /**
     * 转换为岗位展示对象。
     *
     * @param entity 岗位实体
     * @return 岗位展示对象
     */
    private PostVO toPostVO(SysPostEntity entity) {
        PostVO vo = new PostVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setPostName(entity.getPostName());
        vo.setPostCode(entity.getPostCode());
        vo.setSort(defaultInt(entity.getSort(), 0));
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    /**
     * 转换为权限展示对象。
     *
     * @param entity 权限实体
     * @param menuName 菜单名称
     * @return 权限展示对象
     */
    private PermissionVO toPermissionVO(SysPermissionEntity entity, String menuName) {
        PermissionVO vo = new PermissionVO();
        vo.setKey(String.valueOf(entity.getId()));
        vo.setPermissionName(entity.getPermissionName());
        vo.setPermissionCode(entity.getPermissionCode());
        vo.setResourceType(entity.getResourceType());
        vo.setMenuName(defaultIfBlank(menuName, "-"));
        vo.setStatus(entity.getStatus());
        return vo;
    }

    /**
     * 转换为关系绑定展示对象。
     *
     * @param entity 关系实体
     * @param userId 用户编号
     * @param roleId 角色编号
     * @param menuId 菜单编号
     * @return 关系绑定展示对象
     */
    private RelationBindingVO toRelationBindingVO(Object entity, Long userId, Long roleId, Long menuId) {
        RelationBindingVO vo = new RelationBindingVO();
        if (entity instanceof SysUserRoleEntity userRoleEntity) {
            vo.setKey(String.valueOf(userRoleEntity.getId()));
            vo.setStatus(userRoleEntity.getStatus());
        } else if (entity instanceof SysRoleMenuEntity roleMenuEntity) {
            vo.setKey(String.valueOf(roleMenuEntity.getId()));
            vo.setStatus(roleMenuEntity.getStatus());
        } else {
            vo.setKey(String.valueOf(DEFAULT_RELATION_ID));
            vo.setStatus(DEFAULT_STATUS);
        }
        vo.setUserId(userId);
        vo.setRoleId(roleId);
        vo.setMenuId(menuId);
        return vo;
    }
}
