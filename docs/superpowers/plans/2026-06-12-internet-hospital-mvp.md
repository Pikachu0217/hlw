# 互联网医院 MVP 实施计划

> **面向代理执行者：** 必须使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans`，按任务逐项实施本计划。步骤使用复选框语法 `- [ ]` 进行跟踪。

**目标：** 为 SaaS 多租户互联网医院平台搭建 MVP 基础能力，覆盖网关与认证、租户与系统管理、医生、预约挂号、在线问诊聊天、处方、药品、订单，以及管理端与患者端前端。

**架构：** 后端采用位于 `code/backend` 下的 Java 17 Spring Boot 3.2 多模块工程；前端采用位于 `code/frontend` 下的 React 18 TypeScript 工作区。各业务模块需保持可独立测试，仅通过 `hospital-common` 共享横切契约，并统一通过 `hospital-gateway` 对外暴露流量。

**技术栈：** Java 17、Spring Boot 3.2、Spring Cloud 2023、Spring Cloud Alibaba 2023、Nacos 2.3、Sa-Token、MyBatis-Plus、PostgreSQL 16、Redis 7、Redisson、RabbitMQ/本地队列、MinIO、React 18、TypeScript、Vite、Ant Design、Ant Design Mobile、Zustand。

---

## 范围与执行规则

- 将 MVP 按四个切片推进：基础设施、核心业务、处方/药品/订单、前端完善与运维验证。
- 数据库工作必须遵循 PRD：使用 PostgreSQL 16、每个服务一个逻辑数据库，并通过 `resources/sql/init.sql` 完成本地初始化。
- 在可行情况下，每个后端任务都要先补测试再实现：服务层使用 JUnit，接口契约使用控制器测试，锁、队列、WebSocket 处理器使用模拟依赖的集成风格测试。
- 每完成一个任务都要提交一次，只提交该任务实际变更的文件。
- 保持模块边界稳定。除非任务明确要求，否则在 API 已被消费后不得在服务间搬移代码。

## 文件结构

创建或修改以下区域：

- `code/backend/pom.xml`：父 Maven 模块与依赖管理。
- `code/backend/hospital-common/*`：共享返回模型、异常、租户上下文、MyBatis、Redis、安全、OSS、MQ 抽象。
- `code/backend/hospital-gateway/*`：Spring Cloud Gateway 路由、认证过滤器、请求日志、限流钩子。
- `code/backend/hospital-auth/*`：登录、令牌签发、用户/角色/菜单 API。
- `code/backend/hospital-system/*`：租户、字典、配置、操作日志。
- `code/backend/hospital-doctor/*`：医生、科室、医生科室定价、排班。
- `code/backend/hospital-patient/*`：患者档案与健康档案。
- `code/backend/hospital-appointment/*`：预约单、号源、放号配置、抢单流程。
- `code/backend/hospital-consult/*`：问诊单、问诊消息、WebSocket 端点、超时调度。
- `code/backend/hospital-prescription/*`：处方、处方项、审核流程。
- `code/backend/hospital-drug/*`：药品目录、库存、配送。
- `code/backend/hospital-order/*`：统一订单、模拟支付、支付成功事件。
- `code/backend/docker-compose.yml`：本地基础设施编排。
- `resources/sql/init.sql`：用于本地 Docker 开发的 PostgreSQL 16 初始化 SQL。
- `code/frontend/package.json`、`pnpm-workspace.yaml`：前端工作区根配置。
- `code/frontend/admin-web/*`：管理端 SPA。
- `code/frontend/patient-h5/*`：患者移动端 H5。
- `docs/superpowers/specs/2026-06-11-internet-hospital-design.md`：来源 PRD，除非需求变化，否则只读。

## 后端 API 与数据约定

所有服务统一使用以下共享契约：

```java
package com.hlw.common.core.domain;

public record R<T>(int code, String message, T data) {
    public static <T> R<T> ok(T data) {
        return new R<>(200, "success", data);
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }
}
```

```java
package com.hlw.common.core.domain;

import java.util.List;

public record PageResult<T>(List<T> records, long total, long pageNum, long pageSize) {
}
```

```java
package com.hlw.common.core.tenant;

public final class TenantContext {
    private static final ThreadLocal<Long> HOLDER = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        HOLDER.set(tenantId);
    }

    public static Long getTenantId() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
```

所有业务表都必须包含以下公共字段：

```sql
tenant_id BIGINT NOT NULL,
create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
create_by BIGINT,
update_by BIGINT,
deleted SMALLINT NOT NULL DEFAULT 0
```

## 任务 1：后端父工程、公共契约与本地基础设施

**文件：**
- 创建：`code/backend/pom.xml`
- 创建：`code/backend/hospital-common/pom.xml`
- 创建：`code/backend/hospital-common/common-core/pom.xml`
- 创建：`code/backend/hospital-common/common-core/src/main/java/com/hlw/common/core/domain/R.java`
- 创建：`code/backend/hospital-common/common-core/src/main/java/com/hlw/common/core/domain/PageResult.java`
- 创建：`code/backend/hospital-common/common-core/src/main/java/com/hlw/common/core/exception/BizException.java`
- 创建：`code/backend/hospital-common/common-core/src/main/java/com/hlw/common/core/tenant/TenantContext.java`
- 创建：`code/backend/hospital-common/common-core/src/test/java/com/hlw/common/core/domain/RTest.java`
- 创建：`code/backend/docker-compose.yml`

- [ ] **步骤 1：编写公共返回对象测试**

```java
package com.hlw.common.core.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RTest {
    @Test
    void ok_wraps_success_payload() {
        R<String> result = R.ok("pong");

        assertThat(result.code()).isEqualTo(200);
        assertThat(result.message()).isEqualTo("success");
        assertThat(result.data()).isEqualTo("pong");
    }

    @Test
    void fail_wraps_error_without_payload() {
        R<String> result = R.fail(401, "未登录");

        assertThat(result.code()).isEqualTo(401);
        assertThat(result.message()).isEqualTo("未登录");
        assertThat(result.data()).isNull();
    }
}
```

- [ ] **步骤 2：运行失败测试**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-common/common-core test -Dtest=RTest
```

预期：由于 Maven 模块和 `R` 类尚不存在，测试应失败。

- [ ] **步骤 3：补齐 Maven 父工程与 common-core 实现**

使用依赖管理统一维护 Spring Boot 3.2.x、Spring Cloud 2023.x、Spring Cloud Alibaba 2023.x、MyBatis-Plus、Sa-Token、Redisson、Lombok、JUnit 5 与 AssertJ 版本。按共享契约章节中的定义，原样实现 `R`、`PageResult`、`BizException`、`TenantContext`。

- [ ] **步骤 4：补充本地基础设施编排文件**

```yaml
services:
  postgres:
    image: postgres:16
    ports:
      - "5432:5432"
    environment:
      POSTGRES_PASSWORD: hospital123
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./resources/sql/init.sql:/docker-entrypoint-initdb.d/init.sql
  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
  nacos:
    image: nacos/nacos-server:v2.3.0
    ports:
      - "8848:8848"
      - "9848:9848"
    environment:
      MODE: standalone
  rabbitmq:
    image: rabbitmq:3-management
    ports:
      - "5672:5672"
      - "15672:15672"
  minio:
    image: minio/minio
    ports:
      - "9000:9000"
      - "9001:9001"
    environment:
      MINIO_ROOT_USER: minio
      MINIO_ROOT_PASSWORD: minio123
    command: server /data --console-address ":9001"

volumes:
  pgdata:
```

- [ ] **步骤 5：运行测试并提交**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-common/common-core test
git add code/backend
git commit -m "chore: bootstrap backend common module"
```

预期：`RTest` 通过。

## 任务 2：PostgreSQL 基线表结构

**文件：**
- 创建：`resources/sql/init.sql`
- 测试：`code/backend/hospital-common/common-core/src/test/java/com/hlw/common/core/schema/PostgresInitSqlTest.java`

- [ ] **步骤 1：编写 PostgreSQL 初始化 SQL 测试**

```java
package com.hlw.common.core.schema;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresInitSqlTest {
    @Test
    void postgres_init_sql_exists_inside_backend_project() throws Exception {
        Path path = Path.of("/Users/pakachuzy/Desktop/zzz/project/hlw/resources/sql/init.sql");

        assertThat(Files.exists(path)).isTrue();
        String sql = Files.readString(path);
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS sys_user");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS con_consult");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS ord_order");
        assertThat(sql).contains("CREATE TABLE IF NOT EXISTS local_message");
    }
}
```

- [ ] **步骤 2：运行失败测试**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-common/common-core test -Dtest=PostgresInitSqlTest
```

预期：由于 `resources/sql/init.sql` 尚不存在，测试应失败。

- [ ] **步骤 3：创建可执行的 PostgreSQL 16 基线 SQL**

将 `resources/sql/init.sql` 编写为 PostgreSQL `psql` 引导脚本，必须创建 PRD 中定义的各服务数据库：

```sql
SELECT 'CREATE DATABASE hospital_auth'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_auth')\gexec
SELECT 'CREATE DATABASE hospital_system'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_system')\gexec
SELECT 'CREATE DATABASE hospital_patient'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_patient')\gexec
SELECT 'CREATE DATABASE hospital_doctor'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_doctor')\gexec
SELECT 'CREATE DATABASE hospital_consult'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_consult')\gexec
SELECT 'CREATE DATABASE hospital_appointment'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_appointment')\gexec
SELECT 'CREATE DATABASE hospital_prescription'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_prescription')\gexec
SELECT 'CREATE DATABASE hospital_drug'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_drug')\gexec
SELECT 'CREATE DATABASE hospital_order'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'hospital_order')\gexec
```

并在各服务数据库中按 PostgreSQL 类型创建所有 PRD 表：

```sql
\connect hospital_auth
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    username VARCHAR(64) NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(32),
    avatar VARCHAR(512),
    status SMALLINT NOT NULL DEFAULT 1,
    user_type VARCHAR(32) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS sys_role (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    name VARCHAR(64) NOT NULL,
    code VARCHAR(64) NOT NULL,
    status SMALLINT NOT NULL DEFAULT 1,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS sys_menu (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL DEFAULT 0,
    parent_id BIGINT NOT NULL DEFAULT 0,
    name VARCHAR(64) NOT NULL,
    perms VARCHAR(128),
    type VARCHAR(16) NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGSERIAL PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    deleted SMALLINT NOT NULL DEFAULT 0
);
CREATE TABLE IF NOT EXISTS local_message (
    id BIGSERIAL PRIMARY KEY,
    topic VARCHAR(128) NOT NULL,
    body TEXT NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retry INTEGER NOT NULL DEFAULT 3,
    next_retry_time TIMESTAMP,
    status VARCHAR(16) NOT NULL,
    error_msg TEXT,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
```

由于 PRD 要求每个服务库都具备本地队列兜底能力，因此每个服务数据库都要重复创建 `local_message` 表。

- [ ] **步骤 4：补齐剩余 PRD 服务表**

在 `hospital_system` 中创建 `sys_tenant`、`sys_dict`、`sys_config`；在 `hospital_patient` 中创建 `pat_patient`、`pat_health_record`；在 `hospital_doctor` 中创建 `doc_department`、`doc_doctor`、`doc_doctor_department`、`doc_schedule`；在 `hospital_consult` 中创建 `con_consult`、`con_message`、`con_consult_image`；在 `hospital_appointment` 中创建 `apt_appointment`、`apt_number_source`、`apt_number_source_release_config`；在 `hospital_prescription` 中创建 `pre_prescription`、`pre_prescription_item`；在 `hospital_drug` 中创建 `drug_info`、`drug_stock`、`drug_delivery`；在 `hospital_order` 中创建 `ord_order`。

字段名必须与 PRD 完全一致；各状态字段旁需通过注释保留枚举值说明；PRD 中的 JSON 字段（如租户 `config`、`consult_type_config`）统一使用 `JSONB`。

- [ ] **步骤 5：通过 PostgreSQL 容器验证 SQL 语法**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
docker compose up -d postgres
docker compose exec -T postgres psql -U postgres -f /docker-entrypoint-initdb.d/init.sql
mvn -pl hospital-common/common-core test -Dtest=PostgresInitSqlTest
git add resources/sql/init.sql code/backend/hospital-common/common-core/src/test/java/com/hlw/common/core/schema/PostgresInitSqlTest.java
git commit -m "feat: add postgres hospital baseline schema"
```

预期：PostgreSQL 能无报错执行 `init.sql`，且 `PostgresInitSqlTest` 通过。

## 任务 3：公共 MyBatis、Redis、安全与 MQ 模块

**文件：**
- 创建：`code/backend/hospital-common/common-mybatis/*`
- 创建：`code/backend/hospital-common/common-redis/*`
- 创建：`code/backend/hospital-common/common-security/*`
- 创建：`code/backend/hospital-common/common-mq/*`
- 测试：`code/backend/hospital-common/common-mq/src/test/java/com/hlw/common/mq/core/LocalMqRetryTest.java`

- [ ] **步骤 1：编写本地 MQ 重试行为测试**

```java
package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class LocalMqRetryTest {
    @Test
    void failed_message_uses_exponential_backoff_until_max_retry() {
        MqMessage message = new MqMessage("order.paid", "{\"orderId\":1}", 0, 1, 3);

        Duration nextDelay = MqRetryPolicy.nextDelay(message);

        assertThat(nextDelay).isEqualTo(Duration.ofSeconds(2));
    }
}
```

- [ ] **步骤 2：运行失败测试**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-common/common-mq test -Dtest=LocalMqRetryTest
```

预期：由于 common MQ 模块尚不存在，测试应失败。

- [ ] **步骤 3：实现公共模块**

创建：

```java
package com.hlw.common.mq.model;

public record MqMessage(String topic, String body, long delayMillis, int retryCount, int maxRetry) {
}
```

```java
package com.hlw.common.mq.core;

import com.hlw.common.mq.model.MqMessage;

import java.time.Duration;

public final class MqRetryPolicy {
    private MqRetryPolicy() {
    }

    public static Duration nextDelay(MqMessage message) {
        long seconds = (long) Math.pow(2, Math.max(0, message.retryCount()));
        return Duration.ofSeconds(seconds);
    }
}
```

同时创建接口 `MqProducer`、`MqConsumerRegistry`、`MqDispatcher`、`MqLocalMessageStore`，以及注解 `@MqConsumer`、`@MqTopic`。在对应模块中补齐 MyBatis 多租户拦截器配置、Redisson 配置，以及 Sa-Token 辅助工具。

- [ ] **步骤 4：运行测试并提交**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-common/common-mq test
git add code/backend/hospital-common
git commit -m "feat: add common infrastructure modules"
```

预期：common MQ 测试通过。

## 任务 4：网关、认证、租户上下文与 RBAC

**文件：**
- 创建：`code/backend/hospital-gateway/*`
- 创建：`code/backend/hospital-auth/*`
- 创建：`code/backend/hospital-system/*`
- 测试：`code/backend/hospital-auth/src/test/java/com/hlw/auth/service/AuthServiceTest.java`
- 测试：`code/backend/hospital-gateway/src/test/java/com/hlw/gateway/filter/TenantHeaderGatewayFilterTest.java`

- [ ] **步骤 1：编写登录认证测试**

```java
package com.hlw.auth.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceTest {
    @Test
    void login_returns_token_and_tenant_id() {
        FakeUserRepository users = new FakeUserRepository();
        users.save(new LoginUser(1L, 100L, "admin", "{noop}admin123", "ADMIN"));
        AuthService service = new AuthService(users, new FakeTokenIssuer());

        LoginResult result = service.login(new LoginCommand("admin", "admin123"));

        assertThat(result.token()).isEqualTo("test-token-1");
        assertThat(result.tenantId()).isEqualTo(100L);
        assertThat(result.userType()).isEqualTo("ADMIN");
    }
}
```

- [ ] **步骤 2：编写租户头透传网关过滤器测试**

```java
package com.hlw.gateway.filter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TenantHeaderGatewayFilterTest {
    @Test
    void token_tenant_is_forwarded_as_header() {
        TenantHeaderGatewayFilter filter = new TenantHeaderGatewayFilter(token -> 100L);

        String tenantHeader = filter.resolveTenantHeader("test-token-1");

        assertThat(tenantHeader).isEqualTo("100");
    }
}
```

- [ ] **步骤 3：运行失败测试**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-auth,hospital-gateway test -Dtest=AuthServiceTest,TenantHeaderGatewayFilterTest
```

预期：由于 auth 与 gateway 模块尚不存在，测试应失败。

- [ ] **步骤 4：实现认证与网关**

补齐 API：

```http
POST /auth/login
GET /auth/profile
POST /auth/logout
GET /system/tenants
POST /system/tenants
GET /system/roles
GET /system/menus
```

网关行为：

```java
String token = request.getHeaders().getFirst("satoken");
Long tenantId = tokenVerifier.resolveTenantId(token);
ServerHttpRequest mutated = request.mutate()
    .header("X-Tenant-Id", String.valueOf(tenantId))
    .build();
```

- [ ] **步骤 5：运行测试并提交**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-auth,hospital-system,hospital-gateway test
git add code/backend
git commit -m "feat: add gateway auth and tenant management"
```

预期：认证与网关测试通过。

## 任务 5：医生、科室、排班与定价规则

**文件：**
- 创建：`code/backend/hospital-doctor/*`
- 测试：`code/backend/hospital-doctor/src/test/java/com/hlw/doctor/service/AppointmentFeePolicyTest.java`

- [ ] **步骤 1：编写定价策略测试**

```java
package com.hlw.doctor.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentFeePolicyTest {
    @Test
    void department_fee_overrides_doctor_and_title_defaults() {
        AppointmentFeePolicy policy = new AppointmentFeePolicy();

        BigDecimal fee = policy.resolve(
            new FeeContext("主任医师", new BigDecimal("80.00"), new BigDecimal("20.00"))
        );

        assertThat(fee).isEqualByComparingTo("20.00");
    }

    @Test
    void doctor_fee_overrides_title_default_when_department_fee_absent() {
        AppointmentFeePolicy policy = new AppointmentFeePolicy();

        BigDecimal fee = policy.resolve(
            new FeeContext("主任医师", new BigDecimal("80.00"), null)
        );

        assertThat(fee).isEqualByComparingTo("80.00");
    }
}
```

- [ ] **步骤 2：运行失败测试**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-doctor test -Dtest=AppointmentFeePolicyTest
```

预期：由于 doctor 模块尚不存在，测试应失败。

- [ ] **步骤 3：实现医生模块**

创建 API：

```http
GET /doctor/departments
POST /doctor/departments
GET /doctor/doctors
POST /doctor/doctors
PUT /doctor/doctors/{id}/status
POST /doctor/doctors/{id}/departments
GET /doctor/schedules
POST /doctor/schedules
```

实现：

```java
package com.hlw.doctor.service;

import java.math.BigDecimal;
import java.util.Map;

public class AppointmentFeePolicy {
    private static final Map<String, BigDecimal> TITLE_DEFAULTS = Map.of(
        "主任医师", new BigDecimal("50.00"),
        "副主任医师", new BigDecimal("30.00"),
        "主治医师", new BigDecimal("20.00"),
        "住院医师", new BigDecimal("10.00")
    );

    public BigDecimal resolve(FeeContext context) {
        if (context.departmentFee() != null) {
            return context.departmentFee();
        }
        if (context.doctorFee() != null) {
            return context.doctorFee();
        }
        return TITLE_DEFAULTS.getOrDefault(context.title(), BigDecimal.ZERO);
    }
}
```

- [ ] **步骤 4：运行测试并提交**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-doctor test
git add code/backend/hospital-doctor
git commit -m "feat: add doctor schedule and pricing"
```

预期：doctor 模块测试通过。

## 任务 6：患者档案与健康档案

**文件：**
- 创建：`code/backend/hospital-patient/*`
- 测试：`code/backend/hospital-patient/src/test/java/com/hlw/patient/service/PatientProfileServiceTest.java`

- [ ] **步骤 1：编写患者档案测试**

```java
package com.hlw.patient.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PatientProfileServiceTest {
    @Test
    void patient_can_update_masked_profile_fields() {
        PatientProfileService service = new PatientProfileService(new InMemoryPatientRepository());

        PatientProfile profile = service.updateProfile(1L, new UpdatePatientProfileCommand("张三", "13812345678", "男"));

        assertThat(profile.name()).isEqualTo("张三");
        assertThat(profile.maskedPhone()).isEqualTo("138****5678");
    }
}
```

- [ ] **步骤 2：运行失败测试**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-patient test -Dtest=PatientProfileServiceTest
```

预期：由于 patient 模块尚不存在，测试应失败。

- [ ] **步骤 3：实现患者模块**

创建 API：

```http
GET /patient/profile
PUT /patient/profile
GET /patient/health-records
POST /patient/health-records
```

手机号脱敏逻辑：

```java
public String maskPhone(String phone) {
    if (phone == null || phone.length() != 11) {
        return phone;
    }
    return phone.substring(0, 3) + "****" + phone.substring(7);
}
```

- [ ] **步骤 4：运行测试并提交**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-patient test
git add code/backend/hospital-patient
git commit -m "feat: add patient profiles"
```

预期：patient 模块测试通过。

## 任务 7：预约、号源、放号与抢单

**文件：**
- 创建：`code/backend/hospital-appointment/*`
- 测试：`code/backend/hospital-appointment/src/test/java/com/hlw/appointment/service/NumberSourceServiceTest.java`
- 测试：`code/backend/hospital-appointment/src/test/java/com/hlw/appointment/service/GrabAppointmentServiceTest.java`

- [ ] **步骤 1：编写号源防超卖测试**

```java
package com.hlw.appointment.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NumberSourceServiceTest {
    @Test
    void lock_and_use_available_number_source_once() {
        InMemoryNumberSourceRepository repository = new InMemoryNumberSourceRepository();
        repository.save(new NumberSource(1L, 10L, 1, NumberSourceStatus.AVAILABLE));
        NumberSourceService service = new NumberSourceService(repository, new InMemoryDistributedLock());

        NumberSource locked = service.lockOne(10L);

        assertThat(locked.status()).isEqualTo(NumberSourceStatus.LOCKED);
        assertThat(service.tryLockSameNumberAgain(1L)).isFalse();
    }
}
```

- [ ] **步骤 2：编写便民门诊抢单测试**

```java
package com.hlw.appointment.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GrabAppointmentServiceTest {
    @Test
    void only_first_doctor_can_grab_convenient_appointment() {
        InMemoryAppointmentRepository repository = new InMemoryAppointmentRepository();
        repository.save(Appointment.convenient(1L, 100L, 20L));
        GrabAppointmentService service = new GrabAppointmentService(repository, new InMemoryDistributedLock());

        boolean first = service.grab(1L, 200L);
        boolean second = service.grab(1L, 201L);

        assertThat(first).isTrue();
        assertThat(second).isFalse();
        assertThat(repository.findById(1L).doctorId()).isEqualTo(200L);
    }
}
```

- [ ] **步骤 3：运行失败测试**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-appointment test -Dtest=NumberSourceServiceTest,GrabAppointmentServiceTest
```

预期：由于 appointment 模块尚不存在，测试应失败。

- [ ] **步骤 4：实现预约 API 与锁控制**

创建 API：

```http
GET /appointment/number-sources
POST /appointment/appointments
POST /appointment/appointments/{id}/pay
POST /appointment/appointments/{id}/check-in
POST /appointment/appointments/{id}/grab
POST /appointment/release-configs
```

锁键必须严格使用：

```java
String grabLockKey = "hlw:grab:appointment:" + appointmentId;
String numberLockKey = "hlw:lock:number:" + scheduleId;
```

- [ ] **步骤 5：运行测试并提交**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-appointment test
git add code/backend/hospital-appointment
git commit -m "feat: add appointments and number source locking"
```

预期：appointment 模块测试通过。

## 任务 8：在线问诊、WebSocket 消息与超时处理

**文件：**
- 创建：`code/backend/hospital-consult/*`
- 测试：`code/backend/hospital-consult/src/test/java/com/hlw/consult/service/ConsultLifecycleServiceTest.java`
- 测试：`code/backend/hospital-consult/src/test/java/com/hlw/consult/ws/ConsultMessageHandlerTest.java`

- [ ] **步骤 1：编写问诊生命周期测试**

```java
package com.hlw.consult.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultLifecycleServiceTest {
    @Test
    void accepting_consult_sets_duration_from_tenant_config() {
        ConsultLifecycleService service = new ConsultLifecycleService(
            new InMemoryConsultRepository(),
            tenantId -> 30
        );

        Consult consult = service.accept(1L, 100L);

        assertThat(consult.status()).isEqualTo(ConsultStatus.IN_PROGRESS);
        assertThat(consult.durationLimit()).isEqualTo(30);
        assertThat(consult.remainingSeconds()).isEqualTo(1800);
    }
}
```

- [ ] **步骤 2：编写 WebSocket 消息格式测试**

```java
package com.hlw.consult.ws;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsultMessageHandlerTest {
    @Test
    void text_message_is_saved_and_broadcast_as_json() {
        ConsultMessageHandler handler = new ConsultMessageHandler(new InMemoryConsultMessageRepository());

        String json = handler.handle(1L, 2L, "{\"type\":\"CHAT\",\"content\":\"你好\",\"contentType\":\"TEXT\"}");

        assertThat(json).contains("\"type\":\"CHAT\"");
        assertThat(json).contains("\"content\":\"你好\"");
        assertThat(json).contains("\"contentType\":\"TEXT\"");
    }
}
```

- [ ] **步骤 3：运行失败测试**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-consult test -Dtest=ConsultLifecycleServiceTest,ConsultMessageHandlerTest
```

预期：由于 consult 模块尚不存在，测试应失败。

- [ ] **步骤 4：实现问诊 API 与 WebSocket 端点**

创建 API：

```http
POST /consult/consults
POST /consult/consults/{id}/accept
POST /consult/consults/{id}/complete
POST /consult/consults/{id}/extend
GET /consult/consults/{id}/messages
```

创建 WebSocket 端点：

```text
ws://host/ws/consult/{consultId}?token=xxx
```

消息持久化字段包括 `consult_id`、`sender_id`、`sender_type`、`content`、`content_type`、`is_read`、`create_time`。增加调度器：当 `IN_PROGRESS` 状态的问诊满足 `remaining_seconds <= 0` 时标记为 `TIMEOUT`；当剩余时长小于等于五分钟时广播告警。

- [ ] **步骤 5：运行测试并提交**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-consult test
git add code/backend/hospital-consult
git commit -m "feat: add online consult lifecycle and messaging"
```

预期：consult 模块测试通过。

## 任务 9：处方、药品、库存、配送与订单流程

**文件：**
- 创建：`code/backend/hospital-prescription/*`
- 创建：`code/backend/hospital-drug/*`
- 创建：`code/backend/hospital-order/*`
- 测试：`code/backend/hospital-prescription/src/test/java/com/hlw/prescription/service/PrescriptionAuditServiceTest.java`
- 测试：`code/backend/hospital-order/src/test/java/com/hlw/order/service/MockPaymentServiceTest.java`

- [ ] **步骤 1：编写处方审核测试**

```java
package com.hlw.prescription.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrescriptionAuditServiceTest {
    @Test
    void audited_prescription_publishes_order_creation_event() {
        RecordingMqProducer producer = new RecordingMqProducer();
        PrescriptionAuditService service = new PrescriptionAuditService(new InMemoryPrescriptionRepository(), producer);

        Prescription prescription = service.approve(1L, 9L, "审核通过");

        assertThat(prescription.status()).isEqualTo(PrescriptionStatus.AUDITED);
        assertThat(producer.lastTopic()).isEqualTo("prescription.audited");
    }
}
```

- [ ] **步骤 2：编写模拟支付测试**

```java
package com.hlw.order.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MockPaymentServiceTest {
    @Test
    void mock_pay_marks_order_paid_and_publishes_event() {
        RecordingMqProducer producer = new RecordingMqProducer();
        MockPaymentService service = new MockPaymentService(new InMemoryOrderRepository(), producer);

        Order order = service.pay(1L, "MOCK_PAY");

        assertThat(order.status()).isEqualTo(OrderStatus.PAID);
        assertThat(order.payMethod()).isEqualTo("MOCK_PAY");
        assertThat(producer.lastTopic()).isEqualTo("order.paid");
    }
}
```

- [ ] **步骤 3：运行失败测试**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-prescription,hospital-order test -Dtest=PrescriptionAuditServiceTest,MockPaymentServiceTest
```

预期：由于 prescription、drug、order 模块尚不存在，测试应失败。

- [ ] **步骤 4：实现 API 与事件流**

创建 API：

```http
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

发布主题必须严格为：

```text
prescription.audited
order.paid
drug.shipped
```

- [ ] **步骤 5：运行测试并提交**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn -pl hospital-prescription,hospital-drug,hospital-order test
git add code/backend/hospital-prescription code/backend/hospital-drug code/backend/hospital-order
git commit -m "feat: add prescription drug and order flow"
```

预期：处方与订单测试通过。

## 任务 10：管理端 Web 基础与管理页面

**文件：**
- 创建：`code/frontend/package.json`
- 创建：`code/frontend/pnpm-workspace.yaml`
- 创建：`code/frontend/admin-web/*`
- 测试：`code/frontend/admin-web/src/pages/doctor/DoctorList.test.tsx`

- [ ] **步骤 1：编写管理端医生列表测试**

```tsx
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { DoctorList } from "./DoctorList";

describe("DoctorList", () => {
  it("renders doctor management table", () => {
    render(<DoctorList doctors={[{ id: 1, name: "李医生", title: "主任医师", consultStatus: "ONLINE" }]} />);

    expect(screen.getByText("李医生")).toBeInTheDocument();
    expect(screen.getByText("主任医师")).toBeInTheDocument();
    expect(screen.getByText("ONLINE")).toBeInTheDocument();
  });
});
```

- [ ] **步骤 2：运行失败测试**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/frontend
pnpm --filter admin-web test -- DoctorList.test.tsx
```

预期：由于前端工作区尚不存在，测试应失败。

- [ ] **步骤 3：实现管理端工作区**

创建页面：

```text
dashboard
tenant
system/users
system/roles
system/menus
doctor
patient
consult
appointment
prescription
drug
order
```

使用 Ant Design 布局，提供顶层导航、基于 token 的路由守卫、自动携带 `satoken` 的 API 客户端，以及租户、医生、预约、处方、药品、订单等模块的列表/详情/编辑流程。

- [ ] **步骤 4：运行测试并提交**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/frontend
pnpm install
pnpm --filter admin-web test
pnpm --filter admin-web build
git add code/frontend
git commit -m "feat: add admin management web"
```

预期：管理端测试与构建通过。

## 任务 11：患者 H5 核心流程

**文件：**
- 创建：`code/frontend/patient-h5/*`
- 测试：`code/frontend/patient-h5/src/pages/appointment/AppointmentConfirm.test.tsx`
- 测试：`code/frontend/patient-h5/src/pages/consult/ConsultChat.test.tsx`

- [ ] **步骤 1：编写预约确认测试**

```tsx
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { AppointmentConfirm } from "./AppointmentConfirm";

describe("AppointmentConfirm", () => {
  it("shows doctor schedule and fee before submit", () => {
    render(<AppointmentConfirm doctorName="李医生" timeSlot="上午" fee="50.00" />);

    expect(screen.getByText("李医生")).toBeInTheDocument();
    expect(screen.getByText("上午")).toBeInTheDocument();
    expect(screen.getByText("50.00")).toBeInTheDocument();
  });
});
```

- [ ] **步骤 2：编写问诊聊天测试**

```tsx
import { render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ConsultChat } from "./ConsultChat";

describe("ConsultChat", () => {
  it("renders remaining time and messages", () => {
    render(<ConsultChat remainingSeconds={300} messages={[{ id: 1, content: "哪里不舒服", contentType: "TEXT" }]} />);

    expect(screen.getByText("05:00")).toBeInTheDocument();
    expect(screen.getByText("哪里不舒服")).toBeInTheDocument();
  });
});
```

- [ ] **步骤 3：运行失败测试**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/frontend
pnpm --filter patient-h5 test -- AppointmentConfirm.test.tsx ConsultChat.test.tsx
```

预期：由于 patient H5 尚不存在，测试应失败。

- [ ] **步骤 4：实现患者 H5 流程**

创建页面：

```text
home
hospital
department
doctor/list
doctor/detail
appointment/confirm
appointment/result
consult/create
consult/chat
prescription/list
order/list
profile
```

使用 Ant Design Mobile 组件、Zustand 的认证/会话状态管理、自动携带 `satoken` 的 API 客户端，以及连接 `/ws/consult/{consultId}?token=xxx` 的 WebSocket 问诊客户端。

- [ ] **步骤 5：运行测试并提交**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/frontend
pnpm --filter patient-h5 test
pnpm --filter patient-h5 build
git add code/frontend/patient-h5
git commit -m "feat: add patient h5 core flows"
```

预期：患者 H5 测试与构建通过。

## 任务 12：MVP 端到端验证与文档

**文件：**
- 创建：`docs/superpowers/reports/2026-06-12-internet-hospital-mvp-verification.md`
- 创建：`code/backend/README.md`
- 创建：`code/frontend/README.md`
- 修改：`code/backend/docker-compose.yml`

- [ ] **步骤 1：编写验证清单**

创建 `docs/superpowers/reports/2026-06-12-internet-hospital-mvp-verification.md`，内容如下：

```markdown
# Internet Hospital MVP Verification

## Backend
- [ ] `mvn test` passes for all backend modules.
- [ ] PostgreSQL 16 executes `resources/sql/init.sql`.
- [ ] Gateway forwards authenticated requests with `X-Tenant-Id`.
- [ ] Appointment number source cannot be oversold.
- [ ] Convenient clinic appointment can only be grabbed once.
- [ ] Consult WebSocket stores and broadcasts text messages.
- [ ] Consult timeout scheduler moves expired consults to `TIMEOUT`.
- [ ] Prescription approval publishes `prescription.audited`.
- [ ] Mock payment publishes `order.paid`.

## Frontend
- [ ] `pnpm --filter admin-web build` passes.
- [ ] `pnpm --filter patient-h5 build` passes.
- [ ] Admin can navigate tenant, doctor, appointment, prescription, drug, and order pages.
- [ ] Patient can choose hospital, department, doctor, appointment slot, and open consult chat.

## Manual Smoke Flow
- [ ] Create tenant.
- [ ] Create admin, doctor, pharmacist, and patient users.
- [ ] Create department, doctor, schedule, and number source.
- [ ] Patient books appointment and pays with `MOCK_PAY`.
- [ ] Doctor grabs convenient clinic appointment.
- [ ] Patient starts consult and sends a message.
- [ ] Doctor completes consult and creates prescription.
- [ ] Pharmacist approves prescription.
- [ ] Patient pays drug order.
- [ ] Drug delivery is marked shipped.
```

- [ ] **步骤 2：执行后端完整验证**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/backend
mvn test
docker compose up -d postgres redis nacos rabbitmq minio
docker compose exec -T postgres psql -U postgres -f /docker-entrypoint-initdb.d/init.sql
```

预期：Maven 测试通过，且 PostgreSQL 表结构可成功应用。

- [ ] **步骤 3：执行前端完整验证**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/frontend
pnpm install
pnpm --filter admin-web test
pnpm --filter admin-web build
pnpm --filter patient-h5 test
pnpm --filter patient-h5 build
```

预期：前端测试与构建通过。

- [ ] **步骤 4：补充 README**

后端 README 必须包含：

```markdown
# Hospital Backend

## Start Infrastructure
`docker compose up -d postgres redis nacos rabbitmq minio`

## Apply Schema
`docker compose exec -T postgres psql -U postgres -f /docker-entrypoint-initdb.d/init.sql`

## Test
`mvn test`
```

前端 README 必须包含：

```markdown
# Hospital Frontend

## Install
`pnpm install`

## Admin
`pnpm --filter admin-web dev`

## Patient H5
`pnpm --filter patient-h5 dev`

## Verify
`pnpm --filter admin-web test && pnpm --filter patient-h5 test`
```

- [ ] **步骤 5：提交验证文档**

执行：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw
git add docs/superpowers/reports code/backend/README.md code/frontend/README.md code/backend/docker-compose.yml
git commit -m "docs: add mvp verification guide"
```

预期：验证报告与启动文档提交完成。

## 自检

规格覆盖情况：

- 多租户 SaaS、租户上下文与网关认证由任务 2、3、4 覆盖。
- 用户、角色、菜单、租户、字典、配置管理由任务 4 覆盖。
- 医生、科室、医生科室定价、排班由任务 5 覆盖。
- 患者档案与健康档案由任务 6 覆盖。
- 预约、号源、定时放号配置、防超卖、便民门诊抢单由任务 7 覆盖。
- 在线问诊、消息、WebSocket、倒计时、提醒、超时处理由任务 8 覆盖。
- 处方、药品、库存、配送、订单、模拟支付、事件流由任务 9 覆盖。
- 管理端 SPA 由任务 10 覆盖。
- 患者 H5 由任务 11 覆盖。
- 本地基础设施、PostgreSQL 基线表结构、验证文档由任务 1、2、12 覆盖。

占位符扫描：

- 不得保留 `TBD`、`TODO`、`implement later` 或 `similar to Task N` 之类的占位描述。
- 每个任务都必须具备明确文件、测试、命令、预期结果与提交节点。

类型一致性：

- 返回包装统一使用 `R<T>`。
- 分页统一使用 `PageResult<T>`。
- 租户透传统一使用 `X-Tenant-Id` 与 `TenantContext`。
- 锁键必须与 PRD 保持一致：`hlw:grab:appointment:{appointmentId}`、`hlw:lock:number:{scheduleId}`。
- 事件主题必须与 PRD 保持一致：`order.paid`、`consult.completed`、`prescription.audited`、`drug.shipped`、`appointment.created`。
