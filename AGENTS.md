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
- SQL 脚本：每个表有 `COMMENT ON TABLE`，每个字段有 `COMMENT ON COLUMN`，全部使用中文。

### 4. 前端样式全部收口到文件中，其他地方均从此处引用
- 所有 CSS 样式必须写在 `.css` 文件（如 `src/styles/global.css`）中，禁止内联 `style={}` 和 CSS-in-JS。
- 组件通过 `className` 引用样式文件中定义的类名。

### 5. 以后新增接口后需要更新接口测试脚本
- 新增任何后端 Controller 接口后，必须在 `resources/scripts/api-test.sh` 的 `run_all_cases()` 中增加对应的测试用例。
- 测试用例应包括正常场景和关键参数。

### 6. 新增功能需要更新 README.md 文档
- 新增模块、接口、页面、数据库表或环境变量后，必须同步更新对应的 README：
  - 后端：`code/backend/README.md`（模块列表、接口清单、端口、数据库、构建命令）
  - 前端：`code/frontend/README.md`（应用列表、页面范围、启动命令）
- 文档更新和代码改动放在同一个提交中。

## 项目结构

- `code/backend/` — Maven 多模块，10 个业务模块 + 公共模块
- `code/frontend/` — pnpm monorepo，`admin-web`（管理端）+ `patient-h5`（患者端）
- `resources/sql/init.sql` — 单一基线建库建表脚本
- `resources/scripts/api-test.sh` — 接口测试脚本
- `resources/scripts/service.sh` — 一键启停脚本

## 关键命令

```bash
# 后端构建（仅用于参考，请勿执行）
cd code/backend && mvn -Dmaven.test.skip=true clean install

# 前端启动
cd code/frontend && pnpm dev:admin   # 管理端 :13200
cd code/frontend && pnpm dev:patient # 患者端 :13300
```
