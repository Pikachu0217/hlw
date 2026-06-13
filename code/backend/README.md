# 互联网医院后端启动文档

本文档是互联网医院后端的长期维护启动文档。以后每新增一个后端模块，或者模块启动方式、端口、依赖、中间件、环境变量发生变化，都必须在同一个任务提交里同步更新本文档。

## 当前阶段

已完成并提交：

- `hospital-common/common-core`：统一响应、分页模型、业务异常、租户上下文。
- `hospital-common/common-mybatis`：MyBatis-Plus 多租户拦截器配置。
- `hospital-common/common-redis`：Redisson 分布式锁辅助服务。
- `hospital-common/common-security`：Sa-Token 辅助工具。
- `hospital-common/common-mq`：本地消息队列抽象与重试策略。
- `sql/init.sql`：PostgreSQL 16 基线建库建表脚本。

本阶段新增：

- `hospital-gateway`：网关租户请求头透传过滤器。
- `hospital-auth`：登录服务与认证接口骨架。
- `hospital-system`：租户、用户、角色、菜单、字典、参数配置、岗位、权限码、用户角色和角色菜单接口骨架。
- `hospital-doctor`：医生、科室、排班接口骨架与挂号费规则。
- `hospital-patient`：患者资料、手机号脱敏和健康记录接口骨架。
- `hospital-appointment`：预约、号源锁定、放号配置和便民门诊抢单骨架。
- `hospital-consult`：在线问诊生命周期、消息处理、WebSocket 端点和超时调度骨架。
- `hospital-prescription`：处方创建、提交、审核和驳回接口骨架。
- `hospital-drug`：药品、库存、发货接口骨架。
- `hospital-order`：统一订单、模拟支付和支付事件骨架。

## 环境要求

- JDK 17 或更高版本，Maven 构建按 Java 17 release 编译。
- Maven 3.6+。
- 完整本地运行需要 PostgreSQL 16、Redis 7、Nacos 2.3、RabbitMQ、MinIO。

中间件由开发者自行安装和管理。本仓库的 `docker-compose.yml` 只作为本地依赖服务、端口和默认账号的参考，不作为必须启动方式。

## 目录结构

```text
code/backend/
├── pom.xml
├── docker-compose.yml
├── sql/
│   └── init.sql
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

后端业务模块骨架已覆盖 PRD 中的核心服务，当前阶段重点转向前端工作台、联调接入、可启动配置、持久化实现和网关路由完善。

## 本地中间件

本地默认端点：

| 组件 | 版本 | 地址 |
| --- | --- | --- |
| PostgreSQL | 16 | `localhost:5432` |
| Redis | 7 | `localhost:6379` |
| Nacos | 2.3 | `localhost:8848` |
| RabbitMQ | 3 management | `localhost:5672`，控制台 `localhost:15672` |
| MinIO | 稳定版本 | API `localhost:9000`，控制台 `localhost:9001` |

`docker-compose.yml` 中记录的默认本地账号：

```text
PostgreSQL 用户：postgres
PostgreSQL 密码：hospital123
MinIO Root 用户：minio
MinIO Root 密码：minio123
```

## 初始化数据库

PostgreSQL 16 启动后，在后端目录执行基线脚本：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
psql -U postgres -f sql/init.sql
```

脚本会创建以下逻辑库：

- `hospital_auth`
- `hospital_system`
- `hospital_patient`
- `hospital_doctor`
- `hospital_consult`
- `hospital_appointment`
- `hospital_prescription`
- `hospital_drug`
- `hospital_order`

每个服务库包含本服务业务表，并包含一张 `local_message` 表，用于本地队列兜底。`common-mq` 在服务上下文存在 `JdbcOperations` 时会优先将本地消息写入该表；没有数据库上下文的单元测试场景保留内存存储。

`hospital_system` 当前已补齐基础管理表：

- `sys_user`：后台用户展示与管理基础表。
- `sys_role`：角色与数据范围基础表。
- `sys_menu`：菜单、路由和权限标识基础表。
- `sys_dict`：字典类型和字典项。
- `sys_config`：系统参数配置。
- `sys_post`：岗位基础资料。
- `sys_user_post`：用户岗位关系。
- `sys_permission`：权限码清单。
- `sys_user_role`：用户角色关系。
- `sys_role_menu`：角色菜单关系。

`resources/sql/init.sql` 保留更完整的领域设计基线，`code/backend/sql/init.sql` 是当前本地联调使用脚本；后续新增字段、表或演示数据时需要同步维护两处口径，并为每个字段补充 `COMMENT ON COLUMN`。

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

执行 PostgreSQL schema 文件存在性测试：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-common/common-core test -Dtest=PostgresInitSqlTest
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

执行预约模块号源锁定与抢单测试：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-appointment -am test
```

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

脚本运行时会在仓库根目录生成 `.runtime/pids` 和 `.runtime/logs`，分别保存进程 pid 与服务日志。脚本只负责项目进程启停，不会自动启动 PostgreSQL、Redis、Nacos、RabbitMQ、MinIO 等本地中间件。

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

- `application.yml`：统一声明端口、服务名、Nacos、数据库、Redis、RabbitMQ 等键位。
- `application-local.yml`：本地开发默认值，主要指向 `127.0.0.1` 与本地 PostgreSQL/Redis/Nacos。
- `application-prod.yml`：生产占位配置，主要通过环境变量注入。

当前阶段这些配置文件用于明确服务启动约定与环境变量命名，并不代表所有模块已经完成可直接启动的 Spring Boot 主类和完整联调。

PRD 规划端口：

| 模块 | 端口 | 当前状态 |
| --- | ---: | --- |
| `hospital-gateway` | 9000 | 已建模块骨架 |
| `hospital-auth` | 9100 | 已建模块骨架 |
| `hospital-system` | 9200 | 已建模块骨架 |
| `hospital-patient` | 9300 | 已建模块骨架 |
| `hospital-doctor` | 9400 | 已建模块骨架 |
| `hospital-consult` | 9500 | 已建模块骨架 |
| `hospital-appointment` | 9600 | 已建模块骨架 |
| `hospital-prescription` | 9700 | 已建模块骨架 |
| `hospital-drug` | 9800 | 已建模块骨架 |
| `hospital-order` | 9900 | 已建模块骨架 |

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
GET /auth/profile
POST /auth/logout
GET /system/tenants
POST /system/tenants
GET /system/users
POST /system/users
GET /system/roles
POST /system/roles
GET /system/menus
POST /system/menus
GET /system/dicts
POST /system/dicts
GET /system/configs
PUT /system/configs/{id}
GET /system/posts
POST /system/posts
GET /system/permissions
POST /system/permissions
GET /system/user-roles
POST /system/user-roles
GET /system/role-menus
POST /system/role-menus
```

认证资料接口会从登录令牌解析用户编号和租户编号，并回查认证库 `sys_user` 返回登录用户资料。系统管理接口已接入 `sys_tenant`、系统库 `sys_user`、`sys_role`、`sys_menu`、`sys_dict`、`sys_config`、`sys_post`、`sys_permission`、`sys_user_role` 和 `sys_role_menu` 表；租户、用户、角色、菜单、字典、岗位、权限码新增接口均会进行基础参数校验后落库，用户角色和角色菜单绑定接口会校验关联数据并写入授权关系。

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

医生管理已接入 `doc_doctor`、`doc_department`、`doc_doctor_department` 和 `doc_schedule` 表；医生创建、医生状态变更、医生科室绑定和排班创建均会写入数据库并返回当前业务记录。接口脚本中的绑定用例使用内置科室 `10`，与初始化数据保持一致。

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

药品库存管理已接入 `drug_info`、`drug_stock`、`drug_delivery` 表，`POST /drug/drugs` 会创建药品资料，`POST /drug/stocks` 会校验药品并写入库存记录，`POST /drug/deliveries/{id}/ship` 会更新配送单状态并发送发货事件。

订单管理已接入 `ord_order` 表，创建订单会写入待支付订单，支付接口会更新支付状态、支付方式和支付时间，并发布 `order.paid` 事件。

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
请求头：satoken: <token>
转发头：X-Tenant-Id: <token 中解析出的租户 ID>
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
