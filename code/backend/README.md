# 互联网医院后端启动文档

本文档是互联网医院后端的长期维护启动文档。以后每新增一个后端模块，或者模块启动方式、端口、依赖、中间件、环境变量发生变化，都必须在同一个任务提交里同步更新本文档。

## 当前阶段

已完成并提交：

- `hospital-common/common-core`：统一响应、分页模型、业务异常、租户上下文和 JWT 租户解析工具。
- `hospital-common/common-mybatis`：MyBatis-Plus 多租户拦截器配置。
- `hospital-common/common-redis`：Redis 通用工具服务与 Redisson 分布式锁辅助服务。
- `hospital-common/common-security`：Sa-Token 辅助工具、JWT 签发解析、BCrypt 密码编码和统一租户上下文过滤器。
- `hospital-common/common-mq`：本地消息队列抽象与重试策略。
- `resources/sql/001-mysql8-baseline.sql`：MySQL 8 基线建库建表脚本。

本阶段新增：

- `hospital-gateway`：网关租户请求头透传过滤器，会基于 JWT 登录令牌生成可信 `X-Tenant-Id`，并新增平台级网关路由配置 CRUD 管理接口。
- `hospital-auth`：登录服务与认证资料接口已无库化，用户资料通过 Feign 回查 `hospital-system`，登录令牌使用 JWT，密码校验使用 BCrypt，登录审计写入系统模块登录日志。
- `hospital-system`：按当前 `hospital_system` schema 重构为类 RuoYi 后台管理模块，覆盖租户、租户套餐、用户、角色、菜单、字典、参数配置、岗位、通知公告、登录日志、操作日志、用户角色和角色菜单，并补齐后台登录信息与路由接口。
- `hospital-doctor`：医生、科室、医生科室绑定、排班和挂号费规则已改造为 MyBatis Plus + DTO/VO 分层实现。
- `hospital-patient`：患者档案、健康档案、风险等级、身份证与就诊信息已改造为 MyBatis Plus + DTO/VO 分层实现。
- `hospital-appointment`：预约单创建、支付、签到、便民门诊抢单、号源锁定和放号配置已改造为 MyBatis Plus + DTO/VO 分层实现。
- `hospital-consult`：在线问诊生命周期、消息处理和 WebSocket 消息落库已改造为 MyBatis Plus + DTO/VO 分层实现。
- `hospital-prescription`：处方创建、提交、审核和驳回已改造为 MyBatis Plus + DTO/VO 分层实现。
- `hospital-drug`：药品目录、库存记录和配送发货已改造为 MyBatis Plus + DTO/VO 分层实现。
- `hospital-order`：统一订单创建、模拟支付、支付事件发布已改造为 MyBatis Plus + DTO/VO 分层实现。

## 环境要求

- JDK 17 或更高版本，Maven 构建按 Java 17 release 编译。
- Maven 3.6+。
- 完整本地运行需要 MySQL 8、Redis 7、Nacos 2.3、RabbitMQ、MinIO。

中间件由开发者自行安装和管理。本仓库的 `docker-compose.yml` 只作为本地依赖服务、端口和默认账号的参考，不作为必须启动方式。

## 目录结构

```text
code/backend/
├── pom.xml
├── hospital-common/
│   ├── common-core/
│   ├── common-mybatis/
│   ├── common-redis/
│   ├── common-security/
│   └── common-mq/
├── hospital-gateway/
├── hospital-auth/
├── hospital-system/
├── hospital-doctor/
├── hospital-patient/
├── hospital-appointment/
├── hospital-consult/
├── hospital-prescription/
├── hospital-drug/
└── hospital-order/
```

后端业务模块骨架已覆盖 PRD 中的核心服务，当前阶段重点转向前端工作台、联调接入、可启动配置、持久化实现和网关路由完善。当前 `hospital-system` 已完成首轮标准分层改造，可作为其他业务模块迁移 `JdbcOperations + Map` 到 `MyBatis Plus + DTO/VO` 的参考样板。

## 本地中间件

本地默认端点：

| 组件 | 版本 | 地址 |
| --- | --- | --- |
| MySQL | 8 | `localhost:23308` |
| Redis | 7 | `localhost:6379` |
| Nacos | 2.3 | `localhost:8848` |
| RabbitMQ | 3 management | `localhost:5672`，控制台 `localhost:15672` |
| MinIO | 稳定版本 | API `localhost:9000`，控制台 `localhost:9001` |

`docker-compose.yml` 中记录的默认本地账号：

```text
MySQL 用户：root
MySQL 密码：root
MinIO Root 用户：minio
MinIO Root 密码：minio123
```

Nacos 公共认证配置：

- 所有后端应用模块都会通过 `spring.config.import` 尝试读取 `hlw-common-auth.yml`，并继续读取各模块同名 Data ID，例如 `hospital-auth.yml`、`hospital-gateway.yml`。
- 公共配置模板维护在 `resources/nacos/hlw-common-auth.yml`，需要在 Nacos 控制台创建同名 Data ID，默认 Group 为 `DEFAULT_GROUP`。
- `hlw.auth.token-name`、`hlw.auth.token-prefix`、`hlw.auth.tenant-header-name` 是网关、认证服务、业务服务和接口测试脚本共享的登录令牌协议配置；本地未创建 Nacos 配置时，代码默认仍按 `Authorization`、`Bearer`、`X-Tenant-Id` 处理。
- Nacos 连接凭据 `HLW_NACOS_USERNAME` 默认值为 `nacos`，但 `HLW_NACOS_PASSWORD` 未设默认值，必须在环境变量中显式注入（本地开发可用 `export HLW_NACOS_PASSWORD=nacos`），否则服务启动时占位符无法解析。生产环境务必使用强口令，避免使用默认值 `nacos`。

## 初始化数据库

MySQL 8 启动后，在仓库根目录执行基线脚本：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw
mysql -uroot -p < resources/sql/001-mysql8-baseline.sql
```

脚本会创建以下逻辑库：

- `hospital_gateway`
- `hospital_system`
- `hospital_patient`
- `hospital_doctor`
- `hospital_consult`
- `hospital_appointment`
- `hospital_prescription`
- `hospital_drug`
- `hospital_order`

除无库化的 `hospital-auth` 外，每个服务库包含本服务业务表，并包含一张 `local_message` 表，用于本地队列兜底。`common-mq` 在服务上下文存在 `JdbcOperations` 时会优先将本地消息写入该表；没有数据库上下文的单元测试场景保留内存存储。

`common-redis` 当前提供 `RedisService` 通用工具能力，覆盖字符串、对象 JSON、Hash、Set、List、ZSet、过期时间、扫描、删除和基于请求标识的轻量分布式锁释放；复杂锁场景继续使用 Redisson 封装。

`hospital_system` 当前已补齐基础管理表：

- `sys_tenant`：租户主数据表，基线脚本默认写入租户 `100 / 明亮互联网医院`，供管理端登录前选择。
- `sys_user`：后台用户展示与管理基础表，业务用户编号统一为 `U_` 加 32 位 UUID 字符串。
- `sys_role`：角色与数据范围基础表。
- `menu`：菜单、路由和权限标识基础表，`tenant_id=0` 的记录作为平台模板菜单，业务租户记录通过 `source_menu_id` 关联来源模板。
- `sys_dict_type`：字典类型表。
- `sys_dict_data`：字典数据表。
- `sys_config`：系统参数配置。
- `sys_post`：岗位基础资料。
- `sys_user_post`：用户岗位关系。
- `sys_user_role`：用户角色关系。
- `sys_role_menu`：角色菜单关系。
- `sys_role_dept`：角色部门数据权限关系。
- `sys_tenant_package`：租户套餐基础表。
- `sys_tenant_package_menu`：租户套餐菜单关系。
- `sys_notice`：通知公告表。
- `sys_login_info`：系统登录日志表。
- `sys_operator_log`：系统操作日志表。

`hospital_gateway` 当前已补齐网关配置表：

- `gw_route_config`：网关路由配置表，统一按平台租户 `0` 存储。当前版本只做配置管理和展示，不替换 `application.yml` 中的 Spring Cloud Gateway 静态路由加载。

`hospital-system` 当前约定补充如下：

- 控制器统一仅接收 DTO、执行参数校验、调用 Service 并返回 `R`。
- Service 统一负责系统管理业务编排、关系绑定校验和 VO 转换。
- Mapper 统一基于 MyBatis Plus `BaseMapper` 承担数据读写，不再在系统模块中保留 `JdbcOperations` 直查实现。
- 系统服务会优先读取网关透传的可信租户头写入租户上下文，只有缺少租户头时才兜底解析登录令牌；默认请求头为 `Authorization: Bearer <token>`，实际名称和前缀以 `hlw.auth` 公共配置为准。无法识别租户时会进入隔离上下文，不再默认落到平台租户。
- 涉及租户、租户套餐等平台级全局配置的列表、详情、新增、更新和删除，会在 Service 中校验平台令牌上下文；登录前租户选项接口不鉴权，仅公开租户编号、企业名称和状态。
- 创建新租户时必须选择租户套餐，系统会在同一事务内按套餐绑定的平台模板菜单复制 `menu` 数据到新 `tenant_id`，创建 `tenant_admin`、`tenant_user` 默认角色，给租户管理员绑定套餐内全部菜单，并创建默认管理员账号 `admin / 123456` 绑定到 `tenant_admin`。
- 系统模块通过本地 MVC 拦截器自动记录 `/system/**` 操作日志并写入 `sys_operator_log`，日志查询接口 `/system/log/**` 会被排除，避免查询日志时递归产生日志。

## 认证与租户上下文

当前认证链路约定如下：

- 登录页可在未登录状态通过 `GET /system/tenant/options` 读取所有启用租户的最小选项信息，用于选择管理端登录租户。
- 登录接口 `POST /auth/login` 优先读取网关透传的可信租户头，缺少请求头时读取请求体中的 `tenantId`，再结合 `username` 和 `password` 通过 Feign 查询 `hospital-system` 的 `sys_user.password` 中的 BCrypt 哈希，默认初始化平台账号为 `hlw_admin / 123456`，租户编号为 `0`。
- 登录成功后返回 JWT，JWT 中包含 `userId`、`tenantId` 和 `userType`，签名密钥统一由 `HLW_JWT_SECRET` 注入。
- 网关只信任 `hlw.auth.token-name` 请求头中的登录令牌解析出的租户编号，普通业务接口会移除外部传入的 `hlw.auth.tenant-header-name` 并重新写入可信租户头；非公开接口必须解析出平台租户 `0` 或正数业务租户才会放行。
- 网关公开接口路径由 `hlw.gateway.public-paths` 配置读取，默认包含 `/auth/login` 和 `/system/tenant/options`；登录令牌请求头、前缀和租户头由 `hlw.auth` 公共配置读取。
- 登录接口属于公开接口，平台账号使用请求体 `tenantId=0` 登录；业务租户账号允许携带正数可信租户头辅助网关透传租户上下文，后端认证优先以该请求头作为账号查询租户条件。
- 业务服务通过 `common-security` 中的 `JwtTenantContextFilter` 写入 `TenantContext`，优先消费网关透传的可信租户头，缺少租户头时再兜底解析 JWT；令牌无效或租户缺失时进入隔离租户 `-1`。

认证与租户相关环境变量：

```bash
HLW_JWT_SECRET=至少32字节的JWT签名密钥
HLW_NACOS_PASSWORD=Nacos登录口令，本地开发可设为nacos，生产必须使用强口令
HLW_API_TOKEN_NAME=Authorization
HLW_API_TOKEN_PREFIX=Bearer
HLW_API_TENANT_HEADER_NAME=X-Tenant-Id
HLW_API_USERNAME=hlw_admin
HLW_API_PASSWORD=123456
HLW_API_TENANT_ID=0
HLW_API_RUN_PLATFORM_CASES=0
HLW_GATEWAY_DB_URL=jdbc:mysql://127.0.0.1:23308/hospital_gateway?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false
HLW_GATEWAY_DB_USERNAME=root
HLW_GATEWAY_DB_PASSWORD=root
```

`resources/sql/001-mysql8-baseline.sql` 是当前 MySQL 8 初始化脚本，兼作现有领域设计基线和本地联调用脚本；每个表和字段必须补充中文 `COMMENT`。

MySQL 8 切换脚本从 `resources/sql/001-mysql8-baseline.sql` 开始按顺序维护，`resources/sql/002-mysql8-system-base-entity.sql` 统一系统模块基础字段，`resources/sql/003-mysql8-menu-tenant-package-bootstrap.sql` 兼容将存量 `sys_menu` 迁移为 `menu` 并补充 `source_menu_id`。后续每次修改 schema 时新增一份独立 SQL 执行文件，不要继续把所有变更堆叠到 `init.sql` 中。

## 构建与测试

你可以按需自行执行以下命令。当前仓库协作约束下，代理默认不会主动执行这些编译或测试命令。

执行当前已实现后端测试：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn test
```

只执行公共模块测试：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-common/common-core,hospital-common/common-mybatis,hospital-common/common-redis,hospital-common/common-security,hospital-common/common-mq -am test
```

执行 MQ 重试策略测试：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-common/common-mq test
```

执行 MySQL schema 文件存在性测试：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-common/common-core test -Dtest=MysqlInitSqlTest
```

执行 Task 4 相关模块测试时，建议带上 `-am`，让 Maven 同时构建本地依赖模块：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-auth,hospital-system,hospital-gateway -am test
```

执行医生模块挂号费规则测试：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-doctor -am test
```

执行患者模块资料脱敏测试：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-patient -am test
```

`hospital-patient` 当前约定补充如下：

- 控制器统一仅接收 DTO、执行参数校验、调用 Service 并返回 `R`。
- Service 统一负责患者档案、健康档案、风险字段与日期字段的业务编排、租户上下文校验和 VO 转换。
- Mapper 统一基于 MyBatis Plus `BaseMapper` 承担数据读写，不再保留 `JdbcOperations` 和内存仓储实现。
- `pat_patient` 与 `pat_health_record` 已在 `resources/sql/init.sql` 中补齐字段和列注释，后续新增字段仍需同步维护该脚本。

执行预约模块号源锁定与抢单测试：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-appointment -am test
```

`hospital-appointment` 当前约定补充如下：

- 控制器统一仅接收预约 DTO、执行参数校验、调用 Service 并返回 `R`。
- Service 统一负责预约单状态流转、号源锁定、放号配置、放号生成号源和 VO 转换。
- Mapper 统一基于 MyBatis Plus `BaseMapper` 承担数据读写，不再保留 `DemoDataQuery`、`JdbcOperations` 和内存仓储实现。
- 写操作统一要求有效业务租户上下文；无有效租户、隔离租户或平台上下文都不能写入预约、号源和放号数据。

执行问诊模块生命周期与消息格式测试：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-consult -am test
```

执行处方、药品、订单模块测试：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-prescription,hospital-drug,hospital-order -am test
```

## 服务启动

仓库根目录提供一键启停脚本，用于统一管理后端 Spring Boot 模块和前端 Vite 应用：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw
./resources/scripts/service.sh
./resources/scripts/service.sh start
./resources/scripts/service.sh stop
./resources/scripts/service.sh restart
./resources/scripts/service.sh status
./resources/scripts/service.sh logs
```

直接执行 `./resources/scripts/service.sh` 会进入交互式菜单：

```text
1 前端
2 后端
3 退出
```

选择后端后，可继续选择：

```text
1 启动服务
2 停止服务
3 日志输出
4 返回上一级
5 退出
```

后端启动、停止、日志输出都会进入服务选择菜单，可选择单个 `hospital-*` 服务、`ALL`、返回上一级或退出。后端菜单中的 `ALL` 只处理后端服务。

脚本默认启动全部后端业务模块，并使用 `local` profile：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend/<模块名>
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

如需只启动指定后端模块，可通过环境变量覆盖：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw
BACKEND_MODULES="hospital-gateway hospital-auth" SKIP_FRONTEND=1 ./resources/scripts/service.sh start
```

如需指定 Spring Profile：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw
SPRING_PROFILES_ACTIVE=prod SKIP_FRONTEND=1 ./resources/scripts/service.sh start
```

脚本运行时会在仓库根目录生成 `.runtime/pids` 和 `.runtime/logs`，分别保存进程 pid 与服务日志。脚本只负责项目进程启停，不会自动启动 MySQL、Redis、Nacos、RabbitMQ、MinIO 等本地中间件。

当前 `hospital-gateway`、`hospital-auth`、`hospital-system` 以及各业务服务模块已具备接口骨架，但尚未全部补齐完整联调能力。后续模块启动方式、端口、依赖或环境变量发生变化时，需要同步更新本文档和 `resources/scripts/service.sh`。

## 日志配置

各后端业务模块统一使用 `logback-spring.xml`，并从 `application.yml` 的 `hlw.log` 读取日志配置。默认日志根目录为：

```text
/Users/pakachuzy/Desktop/zzz/project/hlw/code/backend/log
```

默认目录结构：

```text
code/backend/log/
├── hospital-auth/
│   ├── info/
│   ├── error/
│   └── debug/
└── hospital-appointment/
    ├── info/
    ├── error/
    └── debug/
```

每个级别日志按天拆分，单个文件超过 `10MB` 后继续按序号拆分。可通过以下环境变量覆盖：

```bash
HLW_LOG_PATH=/data/hospital/log
HLW_LOG_MAX_FILE_SIZE=10MB
HLW_LOG_MAX_HISTORY=30
HLW_LOG_TOTAL_SIZE_CAP=2GB
HLW_LOG_ROOT_LEVEL=INFO
```

如需写入 `debug` 日志，需要将 `HLW_LOG_ROOT_LEVEL` 或对应包日志级别调整为 `DEBUG`。

本轮已为以下模块补充基础配置文件目录：

- `hospital-gateway`
- `hospital-auth`
- `hospital-system`
- `hospital-doctor`
- `hospital-patient`
- `hospital-appointment`
- `hospital-consult`
- `hospital-prescription`
- `hospital-drug`
- `hospital-order`

每个模块当前包含三类配置文件：

- `application.yml`：统一声明端口、服务名、Nacos、数据库、Redis、RabbitMQ 等键位，并通过 Nacos Config 引入 `hlw-common-auth.yml` 与模块私有配置。
- `application-local.yml`：本地开发默认值，主要指向 `127.0.0.1` 与本地 MySQL/Redis/Nacos。
- `application-prod.yml`：生产占位配置，主要通过环境变量注入。

当前阶段这些配置文件用于明确服务启动约定与环境变量命名，并不代表所有模块已经完成可直接启动的 Spring Boot 主类和完整联调。

PRD 规划端口：

| 模块 | 端口 | 当前状态 |
| --- | ---: | --- |
| `hospital-gateway` | 19000 | 已建模块骨架 |
| `hospital-auth` | 19100 | 已建模块骨架 |
| `hospital-system` | 19200 | 已建模块骨架 |
| `hospital-patient` | 19300 | 已建模块骨架 |
| `hospital-doctor` | 19400 | 已建模块骨架 |
| `hospital-consult` | 19500 | 已建模块骨架 |
| `hospital-appointment` | 19600 | 已建模块骨架 |
| `hospital-prescription` | 19700 | 已建模块骨架 |
| `hospital-drug` | 19800 | 已建模块骨架 |
| `hospital-order` | 19900 | 已建模块骨架 |

当某个服务模块变为可启动模块时，必须补充：

- 启动命令；
- 必需环境变量；
- 本地端口；
- Nacos 服务名；
- 数据库名；
- Redis、RabbitMQ、MinIO 等依赖；
- 健康检查或冒烟验证接口。

## 当前接口范围

认证与系统管理接口：

```http
POST /auth/login
GET /auth/detail
POST /auth/logout
GET /system/getInfo
GET /system/getRouters
GET /system/tenant/options
GET /system/tenant
POST /system/tenant
GET /system/tenant/{id}
PUT /system/tenant/{id}
DELETE /system/tenant/{id}
GET /system/tenant-package
POST /system/tenant-package
GET /system/tenant-package/{id}
PUT /system/tenant-package/{id}
DELETE /system/tenant-package/{id}
GET /system/user
POST /system/user
GET /system/user/{id}
PUT /system/user/{id}
DELETE /system/user/{id}
GET /system/dept
POST /system/dept
GET /system/dept/{id}
PUT /system/dept/{id}
DELETE /system/dept/{id}
GET /system/role
POST /system/role
GET /system/role/{id}
PUT /system/role/{id}
DELETE /system/role/{id}
GET /system/menu
POST /system/menu
GET /system/menu/{id}
PUT /system/menu/{id}
DELETE /system/menu/{id}
GET /system/dict
POST /system/dict
GET /system/dict/{id}
PUT /system/dict/{id}
DELETE /system/dict/{id}
GET /system/config
POST /system/config
GET /system/config/{id}
PUT /system/config/{id}
DELETE /system/config/{id}
GET /system/post
POST /system/post
GET /system/post/{id}
PUT /system/post/{id}
DELETE /system/post/{id}
GET /system/notice
POST /system/notice
GET /system/notice/{id}
PUT /system/notice/{id}
DELETE /system/notice/{id}
GET /system/log/login
GET /system/log/operator
GET /system/user-role
POST /system/user-role
GET /system/user-role/{id}
DELETE /system/user-role/{id}
GET /system/role-menu
POST /system/role-menu
GET /system/role-menu/{id}
DELETE /system/role-menu/{id}
GET /gateway/route
POST /gateway/route
GET /gateway/route/{id}
PUT /gateway/route/{id}
DELETE /gateway/route/{id}
```

认证资料接口会从登录令牌解析用户编号和租户编号，并通过 Feign 回查 `hospital-system` 的 `sys_user` 返回登录用户资料。登录成功和失败会通过 internal Feign 写入系统模块 `sys_login_info`；退出登录只写入 Redis 黑名单，不再回写数据库。系统管理接口已接入 `sys_tenant`、系统库 `sys_user`、`sys_dept`、`sys_role`、`menu`、`sys_dict_type`、`sys_dict_data`、`sys_config`、`sys_post`、`sys_tenant_package`、`sys_tenant_package_menu`、`sys_notice`、`sys_login_info`、`sys_operator_log`、`sys_user_role` 和 `sys_role_menu` 表；登录前租户选项接口仅返回最小展示字段，租户和租户套餐管理接口仅允许平台租户上下文访问，租户套餐创建和更新接口通过 `menuIds` 覆盖绑定套餐可用菜单，后端会校验平台模板菜单存在、自动补齐父级模板、去重写入 `sys_tenant_package_menu`，删除套餐时同步清理套餐菜单关系；创建租户时会按套餐复制 `tenant_id=0` 的模板菜单到新租户自己的 `menu` 数据，并初始化默认角色、角色菜单权限和管理员账号绑定；用户、部门、角色、菜单、字典、参数配置、岗位、通知公告均提供列表、详情、新增、更新和逻辑删除接口，其中 `/system/menu` 列表按 `parentId` 返回带 `children` 的菜单树；按钮权限统一使用 `menu.perms`，用户角色和角色菜单绑定接口保持创建或覆盖绑定语义，并强制用户、角色、菜单和关系处于当前 `tenant_id`。系统模块会自动采集 `/system/**` 请求的操作日志并写入 `sys_operator_log`，网关路由配置接口已接入 `gw_route_config`，记录按平台租户 `0` 管理，当前不动态刷新网关主链路路由。

`hospital-auth` 当前约定补充如下：

- 控制器统一接收登录命令并返回登录结果或 `UserProfileVO`，不再返回 `Map`。
- Service 负责账号密码校验、令牌解析、登录资料读取和业务异常转换。
- 用户仓储统一通过 OpenFeign 查询 `hospital-system` 内部用户接口，认证服务不再保留 MyBatis、数据源和认证业务表。
- 登录成功和失败会调用 `hospital-system` 的 `/internal/log/login` 写入 `sys_login_info`，该 internal 接口不应通过网关对外暴露，接口测试脚本仍以 `GET /system/log/login` 校验管理端查询结果。

`hospital-gateway` 当前约定补充如下：

- 网关路由配置 CRUD 仅维护 `gw_route_config` 表，用于管理端展示和后续动态路由扩展。
- 路由配置统一写入平台租户 `0`，Service 读写时显式忽略租户行过滤。
- 当前版本仍以 `application.yml` 的 Spring Cloud Gateway 静态路由为实际转发来源，避免影响网关主链路。

医生、科室与排班接口：

```http
GET /doctor/departments
POST /doctor/departments
GET /doctor/doctors
POST /doctor/doctors
PUT /doctor/doctors/{id}/status
POST /doctor/doctors/{id}/departments
GET /doctor/schedules
POST /doctor/schedules
POST /doctor/appointment-fee/resolve
```

医生管理已接入 `doc_doctor`、`doc_department`、`doc_doctor_department` 和 `doc_schedule` 表；医生、科室、医生科室绑定和排班接口已统一改造为 DTO/VO + MyBatis Plus 分层实现，医生状态变更与挂号费计算也已完成服务层收口。接口脚本中的绑定用例使用内置科室 `10`，与初始化数据保持一致。

患者与健康档案接口：

```http
GET /patient/profile
PUT /patient/profile
GET /patient/patients
GET /patient/health-records
POST /patient/health-records
```

患者基础档案已接入 `pat_patient` 表，`GET /patient/profile` 和 `PUT /patient/profile` 均读取或更新首位患者示例档案，并保持手机号脱敏返回。患者健康档案管理已接入 `pat_health_record` 表，`POST /patient/health-records` 会校验患者存在并落库档案标题与摘要。

预约挂号接口：

```http
GET /appointment/appointments
GET /appointment/number-sources
POST /appointment/appointments
POST /appointment/appointments/{id}/pay
POST /appointment/appointments/{id}/check-in
POST /appointment/appointments/{id}/grab
POST /appointment/number-sources/{scheduleId}/lock
POST /appointment/release-configs
```

预约管理已接入 `apt_appointment`、`apt_number_source` 和 `apt_number_source_release_config` 表，创建预约会锁定可用号源并写入预约单，支付、签到、号源锁定和放号配置均改为数据库状态变更。

问诊接口：

```http
GET /consult/consults
POST /consult/consults
POST /consult/consults/{id}/accept
POST /consult/consults/{id}/complete
POST /consult/consults/{id}/extend
GET /consult/consults/{id}/messages
WS /ws/consult/{consultId}
```

问诊管理已接入 `con_consult` 和 `con_message` 表，创建问诊会写入问诊单并按主诉生成患者消息；接单、延长和完成接口均改为数据库状态变更，种子数据不再覆盖运行态状态。WebSocket 收到的新消息也会写入 `con_message`，`GET /consult/consults/{id}/messages` 统一从数据库读取消息记录。`con_consult_image` 仅存在于 `resources/sql/init.sql` 的完整 baseline 中，用于后续图文问诊图片附件扩展；当前前后端消息接口尚未提供图片上传地址和图片排序入参，因此暂不启用该表，避免产生无入口的伪业务数据。

`hospital-consult` 当前约定补充如下：

- 控制器统一仅接收问诊 DTO、调用 Service 并返回 `ConsultVO`，消息查询继续返回 `ConsultMessage`。
- Service 统一负责问诊单创建、问诊单号生成、主诉消息写入、接单、延长、完成、租户上下文校验和 VO 转换。
- Mapper 统一基于 MyBatis Plus `BaseMapper` 承担问诊单和问诊消息数据读写，不再保留 `DemoDataQuery`、`JdbcOperations` 和内存问诊仓储实现。
- WebSocket 消息仓储统一通过 MyBatis Plus 写入 `con_message`；写接口统一要求有效业务租户上下文。

问诊 WebSocket 地址约定：

```text
ws://host/ws/consult/{consultId}?token=xxx
```

问诊超时规则：

```text
IN_PROGRESS 问诊 remaining_seconds <= 0 时标记 TIMEOUT。
remaining_seconds <= 300 时推送五分钟提醒。
```

处方、药品和订单接口：

```http
GET /prescription/prescriptions
POST /prescription/prescriptions
POST /prescription/prescriptions/{id}/submit
POST /prescription/prescriptions/{id}/approve
POST /prescription/prescriptions/{id}/reject
GET /drug/drugs
POST /drug/drugs
GET /drug/stocks
POST /drug/stocks
POST /drug/deliveries/{id}/ship
POST /order/orders
POST /order/orders/{id}/pay
GET /order/orders
```

处方管理已接入 `pre_prescription` 和 `pre_prescription_item` 表，创建处方会写入草稿和药品明细，提交、审核通过、驳回均改为数据库状态变更并保留审核备注。

`hospital-prescription` 当前约定补充如下：

- 控制器统一仅接收处方 DTO、调用 Service 并返回 `PrescriptionVO`。
- Service 统一负责处方草稿创建、处方号生成、药品明细写入、提交审方、审核通过、驳回、租户上下文校验、审核事件发布和 VO 转换。
- Mapper 统一基于 MyBatis Plus `BaseMapper` 承担处方和处方明细数据读写，不再保留 `DemoDataQuery`、`JdbcOperations` 和内存处方仓储实现。
- 写操作统一要求有效业务租户上下文；无有效租户、隔离租户或平台上下文都不能写入处方和审方数据。

药品库存管理已接入 `drug_info`、`drug_stock`、`drug_delivery` 表，`POST /drug/drugs` 会创建药品资料，`POST /drug/stocks` 会校验药品并写入库存记录，`POST /drug/deliveries/{id}/ship` 会更新配送单状态并发送发货事件。

`hospital-drug` 当前约定补充如下：

- 控制器统一仅接收药品 DTO、调用 Service 并返回药品、库存或配送 VO。
- Service 统一负责药品库存预警计算、库存冗余字段刷新、配送状态流转、租户上下文校验、发货事件发布和 VO 转换。
- Mapper 统一基于 MyBatis Plus `BaseMapper` 承担药品、库存和配送数据读写，不再保留 `DemoDataQuery` 和 `JdbcOperations` 直查实现。
- 写操作统一要求有效业务租户上下文；无有效租户、隔离租户或平台上下文都不能写入药品、库存和配送数据。

订单管理已接入 `ord_order` 表，创建订单会写入待支付订单，支付接口会更新支付状态、支付方式和支付时间，并发布 `order.paid` 事件。

`hospital-order` 当前约定补充如下：

- 控制器统一仅接收订单 DTO、调用 Service 并返回 `R<OrderVO>` 或 `R<List<OrderVO>>`。
- Service 统一负责订单业务类型转换、订单号生成、支付状态流转、租户上下文校验、支付事件发布和 VO 转换。
- Mapper 统一基于 MyBatis Plus `BaseMapper` 承担订单数据读写，不再保留 `DemoDataQuery`、`JdbcOperations` 和内存订单仓储实现。
- 写操作统一要求有效业务租户上下文；无有效租户、隔离租户或平台上下文都不能写入订单或支付数据。

本地消息与事件 topic 约定：

```text
prescription.audited
order.paid
drug.shipped
```

`local_message` 表在每个服务库中作为本地消息兜底表使用。`common-mq` 检测到服务上下文存在 `JdbcOperations` 时会优先写入数据库，否则保留内存实现以支撑单元测试和轻量上下文。

预约模块锁 key 约定：

```text
抢单锁：hlw:grab:appointment:{appointmentId}
号源锁：hlw:lock:number:{scheduleId}
```

网关租户透传规则：

```text
请求头：${hlw.auth.token-name}: ${hlw.auth.token-prefix} <token>
转发头：${hlw.auth.tenant-header-name}: <token 中解析出的租户 ID>
业务服务：优先读取 ${hlw.auth.tenant-header-name} 写入 TenantContext，缺少该头时兜底解析 JWT
```

## 文档维护规则

以后每个模块任务，只要出现以下变化，就必须同步更新本文档，并和该任务代码放在同一个提交中：

- 新增或删除模块；
- 服务从“骨架”变为“可启动”；
- 端口、数据库、Nacos 服务名或中间件依赖变化；
- 新增环境变量；
- 新增或调整数据库脚本；
- 启动、构建或验证命令变化。

不要只把启动信息留在聊天记录或临时任务说明里。
