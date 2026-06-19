# hospital-system Schema 重构实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development or inline execution. 本计划按任务逐项实施；项目规则禁止执行编译测试命令，因此验证仅做源代码级静态检查、接口脚本检查和文档检查。

**目标：** 按 `resources/sql/001-mysql8-baseline.sql` 当前 `hospital_system` schema 完全重构 `hospital-system` 模块，并同步必要的认证、公共契约、接口脚本和 README。

**架构：** 后端保持现有 Spring Boot + MyBatis-Plus 的 Controller、Service、Mapper、Converter 分层风格。系统库字段以 schema 为准，`tenant_id` 在系统模块内使用 `String`，`sys_user.user_id` 使用 `U_` 加 32 位 UUID 的业务用户编号；认证 JWT 暂继续使用 `sys_user.id` 作为技术主键，以避免牵连其他业务库。

**Tech Stack:** Java 17、Spring Boot、MyBatis-Plus、OpenFeign、JWT、MySQL 8、Shell 接口脚本。

---

## 执行规则

- 只改 `hospital-system` 及必要相关模块：`hospital-auth`、`hospital-common/common-core`、`resources/scripts/api-test.sh`、`code/backend/README.md`。
- 不执行 `mvn compile`、`mvn test`、`pnpm test` 等编译或测试命令。
- Java public 方法保留中文 Javadoc，Controller 和关键 Service 方法保留 `log.info`/`log.warn`。
- 按 schema 删除独立权限码模块，按钮权限统一走 `sys_menu.perms`。
- 新增/调整接口后同步更新接口测试脚本和后端 README。

## 任务 1：实体、Mapper 与公共契约贴合 schema

**Files:**
- Modify: `code/backend/hospital-system/src/main/java/com/hlw/system/entity/*.java`
- Modify: `code/backend/hospital-system/src/main/java/com/hlw/system/mapper/*.java`
- Modify: `code/backend/hospital-common/common-core/src/main/java/com/hlw/common/core/domain/system/resp/InternalUserResp.java`
- Delete: `SysPermissionEntity.java`、`SysPermissionMapper.java`

- [x] 将系统实体字段改为当前 `hospital_system` 表字段。
- [x] 新增 `SysDictTypeEntity`、`SysDictDataEntity`、`SysLoginInfoEntity`、`SysNoticeEntity`、`SysOperatorLogEntity`、`SysRoleDeptEntity`、`SysTenantPackageEntity`、`SysTenantPackageMenuEntity` 及 Mapper。
- [x] `InternalUserResp` 同时返回技术主键 `id` 和业务编号 `userId`。

## 任务 2：用户、角色、菜单、授权按 RuoYi 模型重构

**Files:**
- Modify: `UserService`、`RoleService`、`MenuService`、`AuthorizationService`、`InternalUserService`
- Modify: `UserController`、`RoleController`、`MenuController`、`AuthorizationController`、`InternalUserController`
- Modify: 对应 req/resp/converter

- [x] 用户创建时调用用户编号生成方法生成 `U_` + 32 位 UUID。
- [x] 用户角色、用户岗位关联使用 `sys_user.user_id`。
- [x] 角色菜单授权使用 `sys_role_menu`，按钮权限使用 `sys_menu.perms`。
- [x] 提供当前用户 `getInfo`、`getRouters` 需要的权限和菜单聚合能力。

## 任务 3：系统基础数据模块重构

**Files:**
- Modify: `TenantService`、`DeptService`、`PostService`、`DictService`、`ConfigService`
- Create: `TenantPackageService`、`NoticeService`、`LoginInfoService`、`OperatorLogService`
- Modify/Create: 对应 Controller、req、resp、converter

- [x] 租户、套餐、部门、岗位、字典类型/字典数据、参数、公告、登录日志、操作日志接口按 schema 可用。
- [x] 保持现有接口风格，分页返回 `PageResult`，列表/详情/新增/更新/删除使用 `R` 包装。

## 任务 4：认证和公共模块兼容

**Files:**
- Modify: `code/backend/hospital-auth/src/main/java/com/hlw/auth/client/UserFeignClient.java`
- Modify: `code/backend/hospital-auth/src/main/java/com/hlw/auth/domain/resp/LoginUserResp.java`
- Modify: `code/backend/hospital-auth/src/main/java/com/hlw/auth/domain/resp/UserDetailResp.java`
- Modify: `code/backend/hospital-auth/src/main/java/com/hlw/auth/service/FeignUserRepository.java`

- [x] 登录仍按租户编号和账号查询用户。
- [x] JWT 签发仍使用技术主键 `id`，用户资料额外返回业务编号 `userId`。
- [x] 保持 `/internal/users` 和 `/internal/users/{id}` 对认证服务兼容。

## 任务 5：文档、接口脚本与静态验证

**Files:**
- Modify: `resources/scripts/api-test.sh`
- Modify: `code/backend/README.md`

- [x] 接口脚本补充系统管理、租户套餐、公告、日志、授权接口用例。
- [x] README 更新系统模块接口清单和用户编号规则。
- [x] 使用 `rg` 静态检查 `SysPermission`、`sys_permission` 残留。
- [x] 使用 `rg` 静态检查 `hospital-system` 中旧字段名如 `routePath`、`permission`、`sort`、`configType` 是否仍错误映射表字段。
