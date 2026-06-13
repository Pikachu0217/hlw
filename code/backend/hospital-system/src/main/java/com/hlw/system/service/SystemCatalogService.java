package com.hlw.system.service;

import com.hlw.common.core.exception.BizException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 系统基础管理服务，集中处理用户、角色、菜单、岗位、字典、配置和权限授权数据。
 */
@Service
public class SystemCatalogService {
    private static final Logger log = LoggerFactory.getLogger(SystemCatalogService.class);
    private static final long DEFAULT_TENANT_ID = 100L;
    private static final Set<String> MANAGED_TABLES = Set.of(
        "sys_tenant",
        "sys_dict",
        "sys_post",
        "sys_user_role",
        "sys_role_menu",
        "sys_user",
        "sys_role",
        "sys_menu"
    );
    private static final Set<String> RELATION_COLUMNS = Set.of("user_id", "role_id", "menu_id");

    private final JdbcOperations jdbcOperations;

    /**
     * 构造系统基础管理服务。
     *
     * @param jdbcOperations JDBC 操作组件
     */
    public SystemCatalogService(JdbcOperations jdbcOperations) {
        this.jdbcOperations = jdbcOperations;
    }

    /**
     * 查询后台用户列表。
     *
     * @return 后台用户列表
     */
    public List<Map<String, Object>> listUsers() {
        log.info("查询系统用户列表");
        return jdbcOperations.queryForList("""
            SELECT u.id::text AS key,
                   u.username AS username,
                   u.dept_name AS "deptName",
                   u.role_name AS "roleName",
                   COALESCE(p.post_name, '-') AS "postName",
                   u.phone AS phone,
                   u.last_login AS "lastLogin",
                   u.status AS status
            FROM sys_user u
            LEFT JOIN sys_user_post up ON up.user_id = u.id AND up.deleted = 0
            LEFT JOIN sys_post p ON p.id = up.post_id AND p.deleted = 0
            WHERE u.deleted = 0
            ORDER BY u.id
            """);
    }

    /**
     * 查询角色列表。
     *
     * @return 角色列表
     */
    public List<Map<String, Object>> listRoles() {
        log.info("查询系统角色列表");
        return jdbcOperations.queryForList("""
            SELECT r.id::text AS key,
                   r.role_name AS "roleName",
                   r.role_code AS "roleCode",
                   r.data_scope AS "dataScope",
                   COUNT(DISTINCT ur.user_id)::int AS "memberCount",
                   to_char(r.update_time, 'YYYY-MM-DD HH24:MI') AS "updatedAt",
                   r.status AS status
            FROM sys_role r
            LEFT JOIN sys_user_role ur ON ur.role_id = r.id AND ur.deleted = 0
            WHERE r.deleted = 0
            GROUP BY r.id, r.role_name, r.role_code, r.data_scope, r.update_time, r.status
            ORDER BY r.id
            """);
    }

    /**
     * 查询菜单列表。
     *
     * @return 菜单列表
     */
    public List<Map<String, Object>> listMenus() {
        log.info("查询系统菜单列表");
        return jdbcOperations.queryForList("""
            SELECT id::text AS key,
                   parent_id::text AS "parentId",
                   menu_name AS "menuName",
                   menu_type AS "menuType",
                   permission AS permission,
                   route_path AS "routePath",
                   sort AS sort,
                   status AS status
            FROM sys_menu
            WHERE deleted = 0
            ORDER BY parent_id, sort, id
            """);
    }

    /**
     * 查询字典列表。
     *
     * @return 字典列表
     */
    public List<Map<String, Object>> listDicts() {
        log.info("查询系统字典列表");
        return jdbcOperations.queryForList("""
            SELECT id::text AS key,
                   dict_type AS "dictType",
                   dict_label AS "dictLabel",
                   dict_value AS "dictValue",
                   sort AS sort,
                   status AS status,
                   remark AS remark
            FROM sys_dict
            WHERE deleted = 0
            ORDER BY dict_type, sort, id
            """);
    }

    /**
     * 查询参数配置列表。
     *
     * @return 参数配置列表
     */
    public List<Map<String, Object>> listConfigs() {
        log.info("查询系统参数配置列表");
        return jdbcOperations.queryForList("""
            SELECT id::text AS key,
                   config_key AS "configKey",
                   config_value AS "configValue",
                   config_type AS "configType",
                   status AS status,
                   remark AS remark
            FROM sys_config
            WHERE deleted = 0
            ORDER BY id
            """);
    }

    /**
     * 查询岗位列表。
     *
     * @return 岗位列表
     */
    public List<Map<String, Object>> listPosts() {
        log.info("查询系统岗位列表");
        return jdbcOperations.queryForList("""
            SELECT id::text AS key,
                   post_name AS "postName",
                   post_code AS "postCode",
                   sort AS sort,
                   status AS status,
                   remark AS remark
            FROM sys_post
            WHERE deleted = 0
            ORDER BY sort, id
            """);
    }

    /**
     * 查询权限码列表。
     *
     * @return 权限码列表
     */
    public List<Map<String, Object>> listPermissions() {
        log.info("查询系统权限码列表");
        return jdbcOperations.queryForList("""
            SELECT p.id::text AS key,
                   p.permission_name AS "permissionName",
                   p.permission_code AS "permissionCode",
                   p.resource_type AS "resourceType",
                   COALESCE(m.menu_name, '-') AS "menuName",
                   p.status AS status
            FROM sys_permission p
            LEFT JOIN sys_menu m ON m.id = p.menu_id AND m.deleted = 0
            WHERE p.deleted = 0
            ORDER BY p.id
            """);
    }

    /**
     * 查询角色菜单授权列表。
     *
     * @return 角色菜单授权列表
     */
    public List<Map<String, Object>> listRoleMenus() {
        log.info("查询角色菜单授权列表");
        return jdbcOperations.queryForList("""
            SELECT rm.id::text AS key,
                   r.role_name AS "roleName",
                   m.menu_name AS "menuName",
                   m.permission AS permission,
                   rm.status AS status
            FROM sys_role_menu rm
            JOIN sys_role r ON r.id = rm.role_id AND r.deleted = 0
            JOIN sys_menu m ON m.id = rm.menu_id AND m.deleted = 0
            WHERE rm.deleted = 0
            ORDER BY rm.id
            """);
    }

    /**
     * 查询用户角色授权列表。
     *
     * @return 用户角色授权列表
     */
    public List<Map<String, Object>> listUserRoles() {
        log.info("查询用户角色授权列表");
        return jdbcOperations.queryForList("""
            SELECT ur.id::text AS key,
                   u.username AS username,
                   r.role_name AS "roleName",
                   ur.status AS status
            FROM sys_user_role ur
            JOIN sys_user u ON u.id = ur.user_id AND u.deleted = 0
            JOIN sys_role r ON r.id = ur.role_id AND r.deleted = 0
            WHERE ur.deleted = 0
            ORDER BY ur.id
            """);
    }

    /**
     * 创建租户。
     *
     * @param command 租户创建命令
     * @return 创建后的租户数据
     */
    @Transactional
    public Map<String, Object> createTenant(Map<String, Object> command) {
        String tenantName = requiredString(command, "tenantName", "租户名称不能为空");
        String packageName = requiredString(command, "packageName", "套餐名称不能为空");
        String adminName = requiredString(command, "adminName", "管理员名称不能为空");
        String expireAt = requiredString(command, "expireAt", "到期日期不能为空");
        String status = stringValue(command, "status", "正常");
        log.info("创建租户，tenantName={}，packageName={}，adminName={}", tenantName, packageName, adminName);
        Long id = nextId("sys_tenant");
        Long tenantId = longValue(command, "tenantId", id + 100L);
        jdbcOperations.update("""
            INSERT INTO sys_tenant (id, tenant_id, name, tenant_name, package_name, admin_name, expire_at, status)
            VALUES (?, ?, ?, ?, ?, ?, to_date(?, 'YYYY-MM-DD'), ?)
            """, id, tenantId, tenantName, tenantName, packageName, adminName, expireAt, status);
        return row("key", String.valueOf(id), "tenantId", tenantId, "tenantName", tenantName,
            "packageName", packageName, "adminName", adminName, "expireAt", expireAt, "status", status);
    }

    /**
     * 创建字典项。
     *
     * @param command 字典创建命令
     * @return 创建后的字典数据
     */
    @Transactional
    public Map<String, Object> createDict(Map<String, Object> command) {
        String dictType = requiredString(command, "dictType", "字典类型不能为空");
        String dictLabel = requiredString(command, "dictLabel", "字典标签不能为空");
        String dictValue = requiredString(command, "dictValue", "字典键值不能为空");
        int sort = intValue(command, "sort", 0);
        String status = stringValue(command, "status", "启用");
        String remark = stringValue(command, "remark", "");
        log.info("创建字典项，dictType={}，dictLabel={}", dictType, dictLabel);
        Long id = nextId("sys_dict");
        jdbcOperations.update("""
            INSERT INTO sys_dict (id, tenant_id, dict_type, dict_label, dict_value, sort, status, remark)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """, id, DEFAULT_TENANT_ID, dictType, dictLabel, dictValue, sort, status, remark);
        return row("key", String.valueOf(id), "dictType", dictType, "dictLabel", dictLabel, "dictValue", dictValue,
            "sort", sort, "status", status, "remark", remark);
    }

    /**
     * 创建岗位。
     *
     * @param command 岗位创建命令
     * @return 创建后的岗位数据
     */
    @Transactional
    public Map<String, Object> createPost(Map<String, Object> command) {
        String postName = requiredString(command, "postName", "岗位名称不能为空");
        String postCode = requiredString(command, "postCode", "岗位编码不能为空");
        int sort = intValue(command, "sort", 0);
        String status = stringValue(command, "status", "启用");
        String remark = stringValue(command, "remark", "");
        log.info("创建岗位，postName={}，postCode={}", postName, postCode);
        Long id = nextId("sys_post");
        jdbcOperations.update("""
            INSERT INTO sys_post (id, tenant_id, post_name, post_code, sort, status, remark)
            VALUES (?, ?, ?, ?, ?, ?, ?)
            """, id, DEFAULT_TENANT_ID, postName, postCode, sort, status, remark);
        return row("key", String.valueOf(id), "postName", postName, "postCode", postCode, "sort", sort,
            "status", status, "remark", remark);
    }

    /**
     * 更新系统配置值。
     *
     * @param id 配置编号
     * @param command 配置更新命令
     * @return 更新后的配置数据
     */
    @Transactional
    public Map<String, Object> updateConfig(Long id, Map<String, Object> command) {
        String configValue = requiredString(command, "configValue", "配置值不能为空");
        String remark = stringValue(command, "remark", "");
        log.info("更新系统配置，configId={}", id);
        int updated = jdbcOperations.update("""
            UPDATE sys_config
            SET config_value = ?, remark = ?, update_time = CURRENT_TIMESTAMP
            WHERE id = ? AND deleted = 0
            """, configValue, remark, id);
        if (updated == 0) {
            throw new BizException(404, "系统配置不存在");
        }
        return jdbcOperations.queryForMap("""
            SELECT id::text AS key,
                   config_key AS "configKey",
                   config_value AS "configValue",
                   config_type AS "configType",
                   status AS status,
                   remark AS remark
            FROM sys_config
            WHERE id = ?
            """, id);
    }

    /**
     * 绑定用户角色。
     *
     * @param command 用户角色绑定命令
     * @return 绑定结果
     */
    @Transactional
    public Map<String, Object> bindUserRole(Map<String, Object> command) {
        Long userId = requiredLong(command, "userId", "用户编号不能为空");
        Long roleId = requiredLong(command, "roleId", "角色编号不能为空");
        log.info("绑定用户角色，userId={}，roleId={}", userId, roleId);
        assertExists("sys_user", userId, "用户不存在");
        assertExists("sys_role", roleId, "角色不存在");
        Map<String, Object> existing = findRelation("sys_user_role", "user_id", userId, "role_id", roleId);
        if (!existing.isEmpty()) {
            log.info("用户角色已绑定，userId={}，roleId={}", userId, roleId);
            return existing;
        }
        Long id = nextId("sys_user_role");
        jdbcOperations.update("""
            INSERT INTO sys_user_role (id, tenant_id, user_id, role_id, status)
            VALUES (?, ?, ?, ?, '启用')
            """, id, DEFAULT_TENANT_ID, userId, roleId);
        return row("key", String.valueOf(id), "userId", userId, "roleId", roleId, "status", "启用");
    }

    /**
     * 绑定角色菜单。
     *
     * @param command 角色菜单绑定命令
     * @return 绑定结果
     */
    @Transactional
    public Map<String, Object> bindRoleMenu(Map<String, Object> command) {
        Long roleId = requiredLong(command, "roleId", "角色编号不能为空");
        Long menuId = requiredLong(command, "menuId", "菜单编号不能为空");
        log.info("绑定角色菜单，roleId={}，menuId={}", roleId, menuId);
        assertExists("sys_role", roleId, "角色不存在");
        assertExists("sys_menu", menuId, "菜单不存在");
        Map<String, Object> existing = findRelation("sys_role_menu", "role_id", roleId, "menu_id", menuId);
        if (!existing.isEmpty()) {
            log.info("角色菜单已绑定，roleId={}，menuId={}", roleId, menuId);
            return existing;
        }
        Long id = nextId("sys_role_menu");
        jdbcOperations.update("""
            INSERT INTO sys_role_menu (id, tenant_id, role_id, menu_id, status)
            VALUES (?, ?, ?, ?, '启用')
            """, id, DEFAULT_TENANT_ID, roleId, menuId);
        return row("key", String.valueOf(id), "roleId", roleId, "menuId", menuId, "status", "启用");
    }

    /**
     * 获取指定表的下一个演示主键。
     *
     * @param tableName 表名
     * @return 下一个主键
     */
    private Long nextId(String tableName) {
        assertManagedTable(tableName);
        return jdbcOperations.queryForObject("SELECT COALESCE(MAX(id), 0) + 1 FROM " + tableName, Long.class);
    }

    /**
     * 校验记录存在。
     *
     * @param tableName 表名
     * @param id 主键编号
     * @param message 不存在时的错误消息
     */
    private void assertExists(String tableName, Long id, String message) {
        assertManagedTable(tableName);
        Integer count = jdbcOperations.queryForObject(
            "SELECT COUNT(1) FROM " + tableName + " WHERE id = ? AND deleted = 0",
            Integer.class,
            id
        );
        if (count == null || count == 0) {
            throw new BizException(404, message);
        }
    }

    /**
     * 查询已存在的授权关系，避免接口测试重复插入授权数据。
     *
     * @param tableName 表名
     * @param firstColumn 第一个关系字段
     * @param firstValue 第一个关系值
     * @param secondColumn 第二个关系字段
     * @param secondValue 第二个关系值
     * @return 已存在关系，不存在返回空 Map
     */
    private Map<String, Object> findRelation(
        String tableName,
        String firstColumn,
        Long firstValue,
        String secondColumn,
        Long secondValue
    ) {
        assertManagedTable(tableName);
        assertRelationColumn(firstColumn);
        assertRelationColumn(secondColumn);
        List<Map<String, Object>> rows = jdbcOperations.queryForList(
            "SELECT id::text AS key, status AS status FROM " + tableName
                + " WHERE " + firstColumn + " = ? AND " + secondColumn + " = ? AND deleted = 0 ORDER BY id LIMIT 1",
            firstValue,
            secondValue
        );
        return rows.isEmpty() ? Map.of() : rows.get(0);
    }

    /**
     * 校验表名属于系统管理服务内部白名单。
     *
     * @param tableName 表名
     */
    private void assertManagedTable(String tableName) {
        if (!MANAGED_TABLES.contains(tableName)) {
            throw new BizException(500, "系统管理表未纳入白名单");
        }
    }

    /**
     * 校验关系字段属于系统管理服务内部白名单。
     *
     * @param columnName 字段名
     */
    private void assertRelationColumn(String columnName) {
        if (!RELATION_COLUMNS.contains(columnName)) {
            throw new BizException(500, "系统管理关系字段未纳入白名单");
        }
    }

    /**
     * 读取必填字符串。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param message 错误消息
     * @return 字段值
     */
    private String requiredString(Map<String, Object> command, String key, String message) {
        String value = stringValue(command, key, "");
        if (value.isBlank()) {
            throw new BizException(400, message);
        }
        return value;
    }

    /**
     * 读取可选字符串。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param defaultValue 默认值
     * @return 字段值
     */
    private String stringValue(Map<String, Object> command, String key, String defaultValue) {
        Object value = command.get(key);
        if (value == null) {
            return defaultValue;
        }
        return Objects.toString(value).trim();
    }

    /**
     * 读取必填长整型。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param message 错误消息
     * @return 字段值
     */
    private Long requiredLong(Map<String, Object> command, String key, String message) {
        Object value = command.get(key);
        if (value == null || Objects.toString(value).isBlank()) {
            throw new BizException(400, message);
        }
        try {
            return Long.parseLong(Objects.toString(value));
        } catch (NumberFormatException ex) {
            throw new BizException(400, message);
        }
    }

    /**
     * 读取整型字段。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param defaultValue 默认值
     * @return 字段值
     */
    private int intValue(Map<String, Object> command, String key, int defaultValue) {
        Object value = command.get(key);
        if (value == null || Objects.toString(value).isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(Objects.toString(value));
        } catch (NumberFormatException ex) {
            throw new BizException(400, key + "必须是数字");
        }
    }

    /**
     * 读取可选长整型。
     *
     * @param command 请求命令
     * @param key 字段名
     * @param defaultValue 默认值
     * @return 字段值
     */
    private Long longValue(Map<String, Object> command, String key, Long defaultValue) {
        Object value = command.get(key);
        if (value == null || Objects.toString(value).isBlank()) {
            return defaultValue;
        }
        try {
            return Long.parseLong(Objects.toString(value));
        } catch (NumberFormatException ex) {
            throw new BizException(400, key + "必须是数字");
        }
    }

    /**
     * 构造有序返回行。
     *
     * @param values 成对键值
     * @return 有序 Map
     */
    private Map<String, Object> row(Object... values) {
        Map<String, Object> row = new LinkedHashMap<>();
        for (int i = 0; i < values.length; i += 2) {
            row.put(Objects.toString(values[i]), values[i + 1]);
        }
        return row;
    }
}
