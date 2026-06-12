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
- `hospital-system`：租户、角色、菜单接口骨架。

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
└── hospital-system/
```

后续 MVP 阶段会继续新增以下服务模块：

- `hospital-patient`
- `hospital-doctor`
- `hospital-consult`
- `hospital-appointment`
- `hospital-prescription`
- `hospital-drug`
- `hospital-order`

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

每个服务库包含本服务业务表，并包含一张 `local_message` 表，用于本地队列兜底。

## 构建与测试

你可以按需自行执行以下命令。

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

## 服务启动

当前 `hospital-gateway`、`hospital-auth`、`hospital-system` 已有模块骨架和接口代码，但尚未加入完整 Spring Boot 启动类、配置文件、Nacos 注册配置和数据库连接配置。后续模块完成到可运行状态时，需要在本节补充实际启动命令。

PRD 规划端口：

| 模块 | 端口 | 当前状态 |
| --- | ---: | --- |
| `hospital-gateway` | 9000 | 已建模块骨架 |
| `hospital-auth` | 9100 | 已建模块骨架 |
| `hospital-system` | 9200 | 已建模块骨架 |
| `hospital-patient` | 9300 | 待实现 |
| `hospital-doctor` | 9400 | 待实现 |
| `hospital-consult` | 9500 | 待实现 |
| `hospital-appointment` | 9600 | 待实现 |
| `hospital-prescription` | 9700 | 待实现 |
| `hospital-drug` | 9800 | 待实现 |
| `hospital-order` | 9900 | 待实现 |

当某个服务模块变为可启动模块时，必须补充：

- 启动命令；
- 必需环境变量；
- 本地端口；
- Nacos 服务名；
- 数据库名；
- Redis、RabbitMQ、MinIO 等依赖；
- 健康检查或冒烟验证接口。

## 当前接口范围

Task 4 引入以下接口路径：

```http
POST /auth/login
GET /auth/profile
POST /auth/logout
GET /system/tenants
POST /system/tenants
GET /system/roles
GET /system/menus
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
