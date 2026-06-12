# 互联网医院系统 PRD 设计文档

## 1. 项目概述

### 1.1 项目背景

开发一套 SaaS 多租户互联网医院平台，支持多家医院入驻，提供在线问诊、预约挂号、电子处方、药品管理等核心功能。

### 1.2 技术栈

| 层级 | 技术选型 | 说明 |
|------|---------|------|
| 后端框架 | Java 17 + Spring Boot 3.2 | 各微服务基础框架 |
| 微服务治理 | Spring Cloud 2023 + Spring Cloud Alibaba 2023 | 服务注册/配置/网关 |
| 服务注册/配置 | Nacos 2.3 | 服务发现 + 动态配置 |
| API 网关 | Spring Cloud Gateway | 路由、鉴权、限流 |
| 熔断限流 | Sentinel | 服务保护 |
| 消息队列 | RabbitMQ / 自研本地队列 (可配置切换) | 异步消息、延迟消息、广播、重试 |
| ORM | MyBatis-Plus | 多租户插件支持 |
| 数据库 | PostgreSQL 16 | 各服务独立数据库 |
| 缓存 | Redis 7 + Redisson | 会话、Token、热点数据、分布式锁 |
| 认证 | Sa-Token | 轻量级权限认证框架 |
| 对象存储 | MinIO（本地）/ 阿里云 OSS（生产） | 图片、文件 |
| 即时通讯 | WebSocket (Spring WebSocket) | 问诊消息推送 |
| 前端框架 | React 18 + TypeScript + Vite | 管理端 + 患者端 |
| UI 组件库 | Ant Design (管理端) / Ant Design Mobile (患者端) | |
| 状态管理 | Zustand | 轻量级 |
| 构建部署 | Maven (后端) + pnpm (前端) | |

### 1.3 用户角色

| 角色 | 说明 |
|------|------|
| 超级管理员 | 管理所有租户、系统级配置 |
| 医院管理员 | 管理本院科室、医生、药品、运营 |
| 医生 | 接诊、开方、管理患者 |
| 药师 | 审方、配药、发药 |
| 患者 | 挂号、问诊、查看处方、购药 |

## 2. 系统架构

### 2.1 架构图

```
                    ┌──────────────┐
                    │   患者端 H5   │
                    │   管理端 SPA  │
                    └──────┬───────┘
                           │
                    ┌──────▼───────┐
                    │  Nginx 反向代理 │
                    └──────┬───────┘
                           │
              ┌────────────▼────────────┐
              │   Spring Cloud Gateway   │
              │   (路由 + 鉴权 + 限流)    │
              └────────────┬────────────┘
                           │
    ┌──────┬───────┬───────┼───────┬───────┬───────┬──────┐
    ▼      ▼       ▼       ▼       ▼       ▼       ▼      ▼
  认证    用户    问诊    预约    处方    药品    订单    管理
  服务    服务    服务    服务    服务    服务    服务    服务
    └──────┴───────┴───────┼───────┴───────┴───────┴──────┘
                           │
              ┌────────────▼────────────┐
              │     Nacos                │
              │  (服务注册 + 配置中心)     │
              └────────────┬────────────┘
                           │
    ┌──────────┬───────────┼───────────┬──────────┐
    ▼          ▼           ▼           ▼          ▼
  PGSQL      Redis     MinIO/OSS  RabbitMQ/本地队列 ElasticSearch
  (主库)     (缓存)    (文件存储)  (可配置切换)   (搜索-可选)
```

### 2.2 微服务拆分

```
internet-hospital/
├── hospital-gateway/              # API 网关服务 (端口 9000)
├── hospital-auth/                 # 认证服务 (端口 9100)
├── hospital-system/               # 系统服务 (端口 9200)
├── hospital-patient/              # 患者服务 (端口 9300)
├── hospital-doctor/               # 医生服务 (端口 9400)
├── hospital-consult/              # 问诊服务 (端口 9500)
├── hospital-appointment/          # 预约挂号服务 (端口 9600)
├── hospital-prescription/         # 处方服务 (端口 9700)
├── hospital-drug/                 # 药品服务 (端口 9800)
├── hospital-order/                # 订单服务 (端口 9900)
└── hospital-common/               # 公共模块
    ├── common-core/               # 通用工具、异常、常量、DTO
    ├── common-mybatis/            # MyBatis 配置、多租户插件
    ├── common-redis/              # Redis + Redisson 配置
    ├── common-security/           # Sa-Token 配置
    ├── common-oss/                # 对象存储抽象
    └── common-mq/                 # 消息队列框架（RabbitMQ / 本地队列可切换）
```

### 2.3 数据库隔离

每个微服务拥有独立的数据库（逻辑隔离，同一个 PG 实例）：

| 数据库 | 服务 | 核心表 |
|--------|------|--------|
| `hospital_auth` | auth | sys_user, sys_role, sys_menu, sys_user_role |
| `hospital_system` | system | sys_tenant, sys_dict, sys_config |
| `hospital_patient` | patient | pat_patient, pat_health_record |
| `hospital_doctor` | doctor | doc_doctor, doc_schedule, doc_department |
| `hospital_consult` | consult | con_consult, con_message, con_consult_image |
| `hospital_appointment` | appointment | apt_appointment, apt_number_source |
| `hospital_prescription` | prescription | pre_prescription, pre_prescription_item |
| `hospital_drug` | drug | drug_info, drug_stock, drug_delivery |
| `hospital_order` | order | ord_order, ord_payment |

> 注：每个服务数据库都包含 `local_message` 表（本地队列框架的兜底消息表），由 common-mq 模块自动管理。

### 2.4 公共字段规范

所有业务表统一包含以下公共字段：

```sql
create_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 创建时间
update_time   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP  -- 更新时间
create_by     BIGINT       -- 创建人ID
update_by     BIGINT       -- 更新人ID
deleted       SMALLINT     NOT NULL DEFAULT 0  -- 逻辑删除（0=正常, 1=已删除）
```

公共字段通过 MyBatis-Plus 的 `MetaObjectHandler` 自动填充，查询时通过 `@TableLogic` 注解自动过滤已删除数据。

### 2.5 多租户方案

采用共享数据库 + tenant_id 字段隔离：

- 每张业务表都有 `tenant_id` 字段
- MyBatis-Plus 的 `TenantLineInnerInterceptor` 自动注入 tenant_id 条件
- 租户上下文通过 JWT Token 中的 tenant_id 传递
- 超级管理员可跨租户操作（通过注解 `@IgnoreTenant`）
- 系统级数据（如菜单、字典）tenant_id 为 0 表示全局共享

**请求流程：**

```
前端请求 → Gateway（解析 Token 中的 tenant_id）
    → 将 tenant_id 放入请求头 X-Tenant-Id
    → 各服务通过 TenantContext (ThreadLocal) 获取 tenant_id
    → MyBatis-Plus TenantLineInnerInterceptor 自动拼接 WHERE tenant_id = ?
```

### 2.5 认证方案（Sa-Token）

```
登录流程：
Patient/Doctor/Admin → POST /auth/login → Sa-Token 生成 SSO Token
    → 返回 Token → 前端存 localStorage
    → 后续请求 Header: satoken: xxx
    → Gateway 校验 Token → 路由到对应服务

权限模型：
Sa-Token 的 @SaCheckPermission 注解 + RBAC
角色：SUPER_ADMIN, HOSPITAL_ADMIN, DOCTOR, PHARMACIST, PATIENT
```

## 3. 核心业务模块

### 3.1 多租户/医院管理模块

**核心概念：**
- 每家入驻医院 = 一个 Tenant
- 超级管理员管理所有租户，医院管理员管理本院
- 租户拥有独立的科室、医生、药品目录

**关键实体：**

```
Tenant（租户/医院）
├── id, name, logo, address, phone, status
├── license_no (医疗机构许可证号)
├── config (JSON: 问诊费分成比例、问诊时间等)
└── expire_time (租户有效期)

Department（科室）
├── id, tenant_id, name, parent_id, sort, status
└── description

TenantConfig（租户配置）
├── consult_fee (默认问诊费)
├── prescription_enabled (是否开启处方)
├── drug_delivery_enabled (是否开启药品邮寄)
└── consult_type_config (JSON，按咨询类型配置时长)
    ├── IMAGE_TEXT: { duration: 30, unit: "min" }  // 图文问诊30分钟
    ├── QUICK: { duration: 15, unit: "min" }       // 快速咨询15分钟
    └── FOLLOW_UP: { duration: 20, unit: "min" }   // 复诊续方20分钟
```

### 3.2 用户与认证模块

**用户体系：**

```
SysUser（统一用户表）
├── id, username, password, phone, avatar, status
├── tenant_id (所属租户)
└── user_type: PATIENT / DOCTOR / PHARMACIST / ADMIN

SysRole（角色）
├── id, tenant_id, name, code, status

SysMenu（菜单/权限）
├── id, parent_id, name, perms, type (DIR/MENU/BUTTON)

SysUserRole（用户角色关联）
```

### 3.3 医生模块

```
Doctor（医生信息）
├── id, user_id, tenant_id, name, avatar
├── title (主任医师/副主任医师/主治医师/住院医师)
├── specialty (擅长)
├── introduction (简介)
├── consult_fee (个人问诊费，可覆盖职称默认)
├── consult_status (在线/忙碌/离线)
└── rating (评分)

DoctorDepartment（医生科室关联，多对多）
├── id, doctor_id, department_id
├── is_free (是否免费科室)
└── appointment_fee (该科室挂号费，覆盖职称默认)

职称挂号费规则（租户可配置）：
├── 主任医师 → 默认挂号费 50元
├── 副主任医师 → 默认挂号费 30元
├── 主治医师 → 默认挂号费 20元
├── 住院医师 → 默认挂号费 10元
└── 优先级：科室自定义 > 医生个人设置 > 租户职称默认

DoctorSchedule（排班）
├── id, doctor_id, tenant_id
├── schedule_date, time_slot (上午/下午)
├── total_number (总号源数)
├── remain_number (剩余号源)
└── status
```

### 3.4 在线问诊模块

**问诊流程：**

```
患者发起问诊 → 选择科室/医生 → 支付问诊费（模拟）→ 创建问诊单
    → 医生收到通知 → 接诊
    → 图文消息交互（支持文字+图片）
    → 医生结束问诊 或 倒计时结束自动关闭 → 可开处方
    → 患者查看问诊记录

**问诊超时机制：**
- 问诊开始时，从租户配置读取该类型的时长限制
- 服务端维护倒计时，WebSocket 推送剩余时间给前端
- 剩余时间 ≤ 5分钟时，提醒双方"问诊即将结束"
- 倒计时归零 → 自动将问诊状态设为 TIMEOUT
- 医生可在超时前手动延长时长（需权限）
```

**关键实体：**

```
Consult（问诊单）
├── id, tenant_id, patient_id, doctor_id
├── consult_no (问诊编号)
├── type: IMAGE_TEXT / QUICK / FOLLOW_UP
├── status: PENDING → IN_PROGRESS → COMPLETED → CANCELLED → TIMEOUT
├── symptoms (主诉)
├── start_time, end_time
├── duration_limit (时长限制，从租户配置读取，单位分钟)
├── remaining_seconds (剩余秒数，实时倒计时)
└── consult_fee

ConsultMessage（消息记录）
├── id, consult_id, sender_id, sender_type
├── content, content_type (TEXT/IMAGE)
├── create_time
└── is_read
```

**WebSocket 设计：**

- 连接地址：`ws://host/ws/consult/{consultId}?token=xxx`
- 消息格式统一为 JSON：`{ type, content, contentType, timestamp }`
- 使用 Sa-Token 的 WebSocket 模块校验 Token

### 3.5 预约挂号模块

**挂号流程：**

```
患者选择医院 → 选择科室 → 选择医生 → 选择日期/时段 → 选择号源
    → 确认预约 → 生成挂号单 → 模拟支付 → 预约成功
    → 到就诊时间 → 签到 → 就诊
```

**关键实体：**

```
Appointment（挂号单）
├── id, tenant_id, patient_id, doctor_id
├── appointment_no (挂号编号)
├── schedule_id (排班ID)
├── appointment_date, time_slot
├── number_seq (挂号序号)
├── status: PENDING → PAID → CHECKED_IN → VISITED → CANCELLED
└── appointment_fee

NumberSource（号源池）
├── id, schedule_id, time_slot
├── number_seq, status (AVAILABLE / LOCKED / USED)
└── lock_time (锁定过期时间)
```

**号源防超卖：** Redisson 分布式锁保障

### 3.6 便民诊室（抢单模式）

**场景：** 患者不指定医生，直接挂号到便民诊室。挂号单出现在该科室所有在线医生的待接诊列表中，任意医生可抢单接诊。

**流程：**
```
患者选择便民诊室 → 支付挂号费（模拟）→ 创建挂号单（无指定医生）
    → 挂号单进入科室所有在线医生的待接诊列表
    → 医生A点击"接诊" → Redis SETNX 抢锁
    → 抢锁成功 → 更新挂号单 doctor_id = 医生A，状态变为已接诊
    → 抢锁失败 → 提示"已被其他医生接诊"
```

**防重复抢单：**
- Redisson 分布式锁：`hlw:grab:appointment:{appointmentId}`
- 加锁成功后检查挂号单状态，未被接诊则更新 doctor_id，已接诊则提示失败
- Redisson 看门狗机制自动续期，防止业务未完成锁已过期

**列表展示区别：**
- 便民诊室挂号单在医生端标记为"便民"标签
- 显示患者主诉信息，方便医生判断是否接诊

### 3.7 定时放号与防超卖

**场景：** 个别科室（如专家号）在指定时间统一放出号源，短时间内大量患者抢号。

**定时放号机制：**
```
NumberSourceReleaseConfig（放号规则）
├── id, tenant_id, department_id, doctor_id (可选)
├── release_time (放号时间，如 08:00)
├── release_days_ahead (提前N天放号)
├── number_count (每次放号数量)
├── time_slot (上午/下午)
└── status

定时任务（Spring Scheduler）：

定时任务（XXL-JOB / Spring Scheduler）：
→ 到达放号时间 → 批量插入号源记录到数据库
→ 同时预热到 Redis：hlw:number:source:{scheduleId} = 可用号源集合
```

**防超卖（Redisson 分布式锁）：**
```
Redisson 分布式锁：hlw:lock:number:{scheduleId}
  → 加锁成功 → 检查剩余号源 → 扣减号源 → 更新数据库 → 释放锁
  → 加锁失败 → 返回"系统繁忙，请稍后重试"
  → Redisson 看门狗自动续期，防止扣减过程中锁过期
```

**抢号流程：**
```
患者选择号源 → 请求到达
  → Redisson 获取分布式锁
  → 成功 → 查询数据库剩余号源 → 扣减 → 更新状态 → 创建挂号单 → 释放锁
  → 失败（锁等待超时） → 返回"系统繁忙，请稍后重试"
```

### 3.8 电子处方模块

**处方流程：**

```
医生开方 → 选择药品 + 用法用量 → 提交处方
    → 药师审核（通过/驳回）
    → 患者确认 → 生成药品订单
    → 药房配药 → 发药/邮寄
```

**关键实体：**

```
Prescription（处方）
├── id, tenant_id, consult_id, doctor_id, patient_id
├── prescription_no
├── diagnosis (诊断)
├── status: DRAFT → PENDING_AUDIT → AUDITED → DISPENSED → REJECTED
├── audit_by, audit_time, audit_remark
└── total_amount

PrescriptionItem（处方明细）
├── id, prescription_id, drug_id
├── drug_name, spec, unit
├── dosage (用量), frequency (频次), days (天数)
├── quantity (数量), unit_price, amount
└── remark (用药备注)
```

### 3.9 药品管理模块

```
DrugInfo（药品目录）
├── id, tenant_id, name, generic_name (通用名)
├── spec (规格), unit, manufacturer
├── category (分类), approval_no (批准文号)
├── prescription_required (是否处方药)
├── price, status

DrugStock（库存）
├── id, tenant_id, drug_id
├── quantity, batch_no, expire_date
└── warehouse_id

DrugDelivery（邮寄记录）
├── id, order_id, tenant_id
├── address, contact_name, contact_phone
├── express_company, express_no
├── status: PENDING → SHIPPED → DELIVERED
└── ship_time, deliver_time
```

### 3.10 订单模块

```
Order（统一订单）
├── id, tenant_id, patient_id
├── order_no, order_type (CONSULT / DRUG)
├── ref_id (关联问诊单ID或处方ID)
├── total_amount, pay_amount
├── status: PENDING → PAID → COMPLETED → CANCELLED → REFUNDED
├── pay_method (模拟: MOCK_PAY)
├── pay_time, expire_time
```

## 4. API 设计规范

### 4.1 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

### 4.2 分页请求/响应

```json
// 请求
GET /api/doctor/list?pageNum=1&pageSize=10&name=张

// 响应
{
  "code": 200,
  "data": {
    "records": [...],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 4.3 错误码规范

| 范围 | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录 |
| 403 | 无权限 |
| 500 | 服务内部错误 |
| 10001-19999 | 认证模块错误 |
| 20001-29999 | 患者模块错误 |
| 30001-39999 | 问诊模块错误 |
| 40001-49999 | 预约模块错误 |
| 50001-59999 | 处方模块错误 |
| 60001-69999 | 药品模块错误 |
| 70001-79999 | 订单模块错误 |

## 5. 服务间通信

| 场景 | 方式 | 说明 |
|------|------|------|
| 同步查询 | OpenFeign | 如问诊服务查询医生信息 |
| 异步事件 | RabbitMQ / 本地队列（可配置切换） | 如问诊完成→通知处方服务、订单支付→通知药品服务发药 |
| 实时消息 | WebSocket | 问诊聊天消息 |

**消息队列支持两种实现，通过配置切换：**

```yaml
# application.yml
hospital:
  mq:
    type: local  # local = 本地队列, rabbitmq = RabbitMQ
```

**RabbitMQ 交换机设计（type=rabbitmq 时使用）：**

```
Exchange: hospital.event (topic)
├── order.paid          → 药品服务（配药）、问诊服务（激活问诊）
├── consult.completed   → 处方服务（可开方通知）
├── prescription.audited → 订单服务（生成药品订单）
├── drug.shipped        → 通知服务（短信/推送）
└── appointment.created → 通知服务（挂号成功通知）
```

**本地队列框架设计（type=local 时使用，基于 Redis ZSET + 本地消息表）：**

```
hospital-common/common-mq/
├── annotation/
│   ├── @MqConsumer          # 消费者注解，标记消费方法
│   └── @MqTopic             # 主题注解
├── core/
│   ├── MqProducer           # 生产者：发送消息、发送延迟消息、广播消息
│   ├── MqConsumerRegistry   # 消费者注册中心
│   ├── MqDispatcher         # 消息分发器
│   └── MqRetryHandler       # 重试处理器
├── model/
│   ├── MqMessage            # 消息体（topic, body, delay, retryCount）
│   └── MqLocalMessage       # 本地消息表实体
├── store/
│   ├── MqRedisStore         # Redis ZSET 存储（就绪队列 + 延迟队列）
│   └── MqLocalMessageStore  # 本地消息表存储（最终保障）
└── scheduler/
    └── MqScanScheduler      # 定时扫描本地消息表，重新投递
```

**三种消息模式：**

| 模式 | 实现 | 说明 |
|------|------|------|
| 直接发送 | Redis List (RPUSH/BLPOP) | 同步或异步投递，消费者阻塞等待 |
| 延迟消息 | Redis ZSET (score=执行时间戳) | 定时轮询 ZSET，到期后转入就绪队列 |
| 广播消息 | 遍历所有消费者组投递 | 每个消费者组都收到一份消息 |

**重试与兜底流程：**

```
消息投递 → 消费者处理
  ├── 成功 → ACK 确认，删除消息
  └── 失败 → 重试（默认3次，指数退避）
       ├── 重试成功 → ACK
       └── 重试失败 → 写入本地消息表 (local_message)
            → 定时任务每30秒扫描 local_message
            → 重新投递到就绪队列
            → 超过最大重试次数 → 标记为死信，人工处理
```

**消息主题设计：**

```
Topic: order.paid          → 药品服务（配药）、问诊服务（激活问诊）
Topic: consult.completed   → 处方服务（可开方通知）
Topic: prescription.audited → 订单服务（生成药品订单）
Topic: drug.shipped        → 通知服务（短信/推送）
Topic: appointment.created → 通知服务（挂号成功通知）
```

**本地消息表结构：**

```
MqLocalMessage（local_message）
├── id, topic, body (JSON)
├── retry_count, max_retry (默认3)
├── next_retry_time (下次重试时间)
├── status: PENDING → RETRYING → SUCCESS → DEAD
├── error_msg (最后一次失败原因)
├── create_time, update_time
```

## 6. 缓存策略

所有缓存 Key 统一加前缀 `hlw:`。

| 数据 | 缓存 Key | 过期时间 | 说明 |
|------|---------|---------|------|
| 用户信息 | `hlw:user:{userId}` | 30min | Sa-Token 会话 |
| 租户配置 | `hlw:tenant:config:{tenantId}` | 1h | 变更时主动清除 |
| 医生列表 | `hlw:doctor:list:{tenantId}:{deptId}` | 10min | 列表缓存 |
| 号源 | `hlw:number:source:{scheduleId}` | 不过期 | Lua 脚本原子操作 |
| 便民抢单锁 | `hlw:grab:appointment:{appointmentId}` | 看门狗续期 | Redisson 防重复抢单 |
| 号源扣减锁 | `hlw:lock:number:{scheduleId}` | 看门狗续期 | Redisson 防超卖 |
| 字典数据 | `hlw:dict:{dictType}` | 24h | 很少变更 |

## 7. 安全设计

- Sa-Token 防止重复登录、Token 被盗
- 接口限流：Sentinel + 自定义注解 `@RateLimit`
- SQL 注入：MyBatis-Plus 参数化查询
- XSS：前端输入过滤 + 后端响应转义
- 敏感数据：手机号、身份证号脱敏存储/展示

## 8. 日志与监控

- **请求日志：** Gateway 层统一记录请求/响应日志
- **业务日志：** 关键操作记录操作日志（登录、开方、支付等）
- **链路追踪：** SkyWalking（可选，后期接入）
- **健康检查：** Spring Boot Actuator + Nacos 健康检查

## 9. 前端设计

### 9.1 管理端 SPA (admin-web)

基于 React 18 + TypeScript + Vite + Ant Design。

**页面结构：**

```
├── dashboard/           # 仪表盘
├── tenant/              # 医院/租户管理
├── system/              # 系统设置（用户、角色、菜单）
├── doctor/              # 医生管理
├── patient/             # 患者管理
├── consult/             # 问诊管理
├── appointment/         # 挂号管理
├── prescription/        # 处方管理
├── drug/                # 药品管理
└── order/               # 订单管理
```

### 9.2 患者端 H5 (patient-h5)

基于 React 18 + TypeScript + Vite + Ant Design Mobile。

**页面结构：**

```
├── home/                # 首页
├── hospital/            # 医院选择
├── department/          # 科室列表
├── doctor/              # 医生列表/详情
├── appointment/         # 预约挂号
├── consult/             # 在线问诊（含聊天界面）
├── prescription/        # 我的处方
├── order/               # 我的订单
└── profile/             # 个人中心
```

## 10. 运维与部署

### 10.1 开发环境

| 组件 | 版本 | 说明 |
|------|------|------|
| JDK | 17 | OpenJDK |
| Node.js | 20 LTS | 前端构建 |
| PostgreSQL | 16 | 数据库 |
| Redis | 7 | 缓存 + 分布式锁 |
| Redisson | 3.27+ | 分布式锁、限流 |
| Nacos | 2.3 | 注册/配置中心 |
| RabbitMQ | 3.13 | 消息队列（可选） |
| 本地队列 | Redis ZSET + 本地消息表 | 消息队列（自研，可选） |
| MinIO | 最新 | 对象存储 |
| Maven | 3.9+ | 后端构建 |
| pnpm | 8+ | 前端包管理 |

### 10.2 Docker Compose（本地开发）

```yaml
services:
  postgres:
    image: postgres:16
    ports: ["5432:5432"]
    environment:
      POSTGRES_PASSWORD: hospital123
    volumes:
      - pgdata:/var/lib/postgresql/data
      - ./sql/init.sql:/docker-entrypoint-initdb.d/init.sql

  redis:
    image: redis:7-alpine
    ports: ["6379:6379"]

  nacos:
    image: nacos/nacos-server:v2.3.0
    ports: ["8848:8848", "9848:9848"]
    environment:
      MODE: standalone

  rabbitmq:
    image: rabbitmq:3-management
    ports: ["5672:5672", "15672:15672"]

  minio:
    image: minio/minio
    ports: ["9000:9000", "9001:9001"]
    command: server /data --console-address ":9001"

volumes:
  pgdata:
```

### 10.3 本地启动顺序

```
1. PostgreSQL → 创建各服务数据库
2. Redis → 启动
3. Nacos → 启动
4. RabbitMQ → 启动（如果使用 RabbitMQ 模式）
5. MinIO → 启动
6. 各微服务 → 按依赖顺序启动
   a. hospital-auth
   b. hospital-system
   c. hospital-patient, hospital-doctor
   d. hospital-consult, hospital-appointment
   e. hospital-prescription, hospital-drug
   f. hospital-order
   g. hospital-gateway (最后)
7. 前端 → pnpm dev
```

### 10.4 生产部署路径

```
阶段一（当前）：本地 Docker Compose 开发
阶段二：服务器 Docker Compose 部署
阶段三：K8s + Helm Chart
  - 每个微服务一个 Deployment
  - HPA 自动扩缩容
  - Ingress 统一入口
  - ConfigMap/Secret 管理配置
```

## 11. MVP 迭代计划

### Phase 1 - 基础框架（2-3 周）

- 项目脚手架搭建（前后端）
- 微服务注册/配置（Nacos）
- 网关 + Sa-Token 认证
- 多租户基础功能
- 用户/角色/权限管理
- Docker Compose 环境

### Phase 2 - 核心业务（3-4 周）

- 医生管理 + 排班
- 预约挂号（含号源管理）
- 在线问诊（图文 + WebSocket）
- 患者端 H5 基础页面

### Phase 3 - 处方与药品（2-3 周）

- 电子处方（开方 + 审方）
- 药品管理 + 库存
- 订单模块（模拟支付）
- 药品邮寄

### Phase 4 - 完善与优化（2 周）

- 管理端仪表盘
- 数据统计报表
- 消息通知
- 性能优化
- 部署文档
