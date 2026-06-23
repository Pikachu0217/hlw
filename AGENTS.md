# 互联网医院（hlw）

Java 17 / Spring Boot / Maven 多模块后端 + React 18 / TypeScript / Vite / Ant Design 前端。

## 项目工作规则

所有会话必须遵守以下 7 条规则：

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

## 项目结构

- `code/backend/` — Maven 多模块，10 个业务模块 + 公共模块
- `code/frontend/` — pnpm monorepo，`admin-web`（管理端）+ `patient-h5`（患者端）
- `resources/sql/001-mysql8-baseline.sql` — MySQL 8 基线建库建表脚本

## 关键命令

```bash
# 后端构建（仅用于参考，请勿执行）
cd code/backend && mvn -Dmaven.test.skip=true clean install

# 前端启动
cd code/frontend && pnpm dev:admin   # 管理端 :13200
cd code/frontend && pnpm dev:patient # 患者端 :13300
```
