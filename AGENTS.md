# 互联网医院（hlw）

Java 17 / Spring Boot / Maven 多模块后端 + React 18 / TypeScript / Vite / Ant Design 前端。

## 项目工作规则

所有会话必须遵守以下 6 条规则：

### 1. 文档和 Git 提交一律使用中文
- 所有文档、注释、提交信息、PR 描述均使用中文（简体）。
- 代码中的标识符（类名、方法名、变量名）保持英文不变。

### 2. 不要编译测试
- 任何时候不执行 `mvn compile`、`mvn test`、`pnpm test` 等编译或测试命令。
- 只做源代码级别的编辑和审查。

### 3. 每个方法、每个数据库表字段都要增加注释，关键方法增加日志输出
- Java 后端：每个 public 方法必须有中文 Javadoc（描述、@param、@return），每个 Entity/DTO/VO 字段必须有行注释。
- 每个 Controller 入口方法和关键 Service 方法必须有 `log.info`/`log.warn` 日志，记录入参、关键分支和异常。
- SQL 脚本：每个表和字段都要补充中文 `COMMENT`。

### 4. 前端样式全部收口到文件中，其他地方均从此处引用
- 所有 CSS 样式必须写在 `.css` 文件（如 `src/styles/global.css`）中，禁止内联 `style={}` 和 CSS-in-JS。
- 组件通过 `className` 引用样式文件中定义的类名。

### 5. 系统模块表格非必须不要使用滑动操作栏
- **非必须的判断标准**：若表格列数 ≤ 6 列（含操作列）且字段均为短文本（如名称、编码、状态、时间），则必须不使用滑动操作栏，操作按钮直接展示。仅在字段较多（≥7 列）或存在长文本字段导致表格总宽超 1400px 时，才允许操作列设为 `fixed: 'right'` 滑动查看。
- `code/frontend/admin-web/src/pages/system/` 下所有表格必须保证操作列在当前可视区域内，不使用横向滚动来查看操作按钮。
- 系统模块表格优先使用紧凑列宽、`table-layout: fixed`、文本省略（`ellipsis`）和操作按钮换行（`white-space: normal`）来适配宽度。操作按钮不超过 3 个时平铺显示，超过 3 个时收起为"更多"下拉菜单。
- 禁止在系统模块表格操作列使用 `fixed: 'right'`；极少数允许使用 `fixed: 'right'` 的场景（见第一条），必须在表格外层包裹容器并设置 `min-width` 保证操作列始终可见，且容器禁止横向溢出。
- 表格样式必须继续收口到 `src/styles/global.css`，页面只通过 `className` 引用。全局样式中统一定义 `.table-action-cell` 类：`white-space: nowrap; text-align: center;`，各页面直接复用。

### 6. 数据库变更必须通过增量 SQL 脚本
- 禁止修改 `001-mysql8-baseline.sql` 基线脚本或已存在的增量脚本（`002`–`012`）。
- 所有数据库 schema 变更（新建表、新增字段、修改字段、新增索引、插入演示数据等）必须按照已有编号顺序新增脚本，命名格式为 `013-mysql8-变更描述.sql`，编号递增。
- 每张新表和每个新字段必须补充中文 `COMMENT`。
- 演示数据插入必须可重复执行，优先使用 `INSERT IGNORE` 或 `ON DUPLICATE KEY UPDATE`。

## 项目结构

### 后端（Maven 多模块）

根目录 `code/backend/`，Spring Boot 3.2.12 / Spring Cloud 2023.0.6 / Spring Cloud Alibaba 2023.0.3.4。

| 模块 | 端口 | 说明 |
|---|---|---|
| `hospital-gateway` | 19000 | 网关、租户头透传、路由配置 |
| `hospital-auth` | 19100 | 登录 / 退出 / 个人资料 |
| `hospital-system` | 19200 | 租户、用户、角色、菜单、字典、配置、岗位、通知、日志 |
| `hospital-patient` | 19300 | 患者档案、健康档案、风险等级 |
| `hospital-doctor` | 19400 | 医生、科室、排班、挂号费规则 |
| `hospital-consult` | 19500 | 在线问诊、WebSocket 消息、咨询工作台 |
| `hospital-appointment` | 19600 | 预约单、号源锁定、放号配置 |
| `hospital-prescription` | 19700 | 处方创建、提交、审核、驳回 |
| `hospital-drug` | 19800 | 药品目录、库存、配送 |
| `hospital-order` | 19900 | 订单创建、模拟支付 |

公共模块（`hospital-common/`）：`common-core`（统一响应、分页、异常、租户上下文）、`common-mybatis`（MyBatis-Plus 多租户）、`common-redis`（Redis + Redisson）、`common-security`（Sa-Token、JWT 签发、BCrypt）、`common-mq`（本地消息队列）。

### 前端（pnpm monorepo）

根目录 `code/frontend/`，React 18 / TypeScript / Vite 5 / Ant Design 5。

| 应用 | 端口 | UI 体系 | 说明 |
|---|---|---|---|
| `admin-web` | 13200 | Ant Design | 管理后台：系统管理、患者、医生、预约、问诊、处方、药品、订单 |
| `patient-h5` | 13300 | Ant Design Mobile + Zustand | 患者端 H5：预约、问诊、处方、药品、订单 |

所有前端 API 请求通过 Vite 代理发送到网关 `http://127.0.0.1:19000`。

### 基础设施与资源

| 路径 | 说明 |
|---|---|
| `resources/sql/` | MySQL 8 数据库脚本：`001-mysql8-baseline.sql`（9 库 30 表基线） + `002`–`012` 增量迁移脚本 |
| `resources/docker/docker-compose.yml` | 本地中间件编排（Redis、Nacos、RabbitMQ、MinIO） |
| `resources/nacos/` | Nacos 配置模板（公共认证、JWT、Sa-Token） |
| `resources/scripts/service.sh` | 一键启停脚本（交互式菜单 / start / stop / restart / status / logs） |
| `resources/scripts/api-test.sh` | 65+ HTTP 接口测试脚本（JSON + Markdown 报告） |
| `resources/开发规范/` | 编码规范：Lombok 使用规范、项目开发规范、通用规范 |
| `docs/` | 设计规格、交付总结、验收审计报告 |

## 关键命令

```bash
# 后端构建（仅用于参考，请勿执行）
cd code/backend && mvn -Dmaven.test.skip=true clean install

# 前端启动
cd code/frontend && pnpm dev:admin   # 管理端 :13200
cd code/frontend && pnpm dev:patient # 患者端 :13300

# 中间件启动
cd resources/docker && docker compose up -d

# 接口测试
bash resources/scripts/api-test.sh           # 交互式菜单
bash resources/scripts/api-test.sh system    # 指定模块测试

# 一键启停
bash resources/scripts/service.sh            # 交互式菜单
bash resources/scripts/service.sh start      # 启动全部后端
```
