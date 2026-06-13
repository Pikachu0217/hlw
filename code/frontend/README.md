# 互联网医院前端工作区说明

本文档用于记录互联网医院前端工作区结构、应用职责、运行命令和后续维护约束。以后每新增一个前端应用、页面模块、工作区脚本或环境变量，都必须在同一个任务提交里同步更新本文档。

## 当前阶段

当前工作区包含两个前端应用：

- `admin-web`：管理端 SPA，面向医院管理后台。
- `patient-h5`：患者端 H5，面向挂号、问诊、处方和订单流程。

## 当前页面范围

`admin-web` 当前已覆盖以下页面骨架：

- `dashboard`
- `tenant`
- `system/users`
- `system/roles`
- `system/menus`
- `system/dicts`
- `system/configs`
- `system/posts`
- `system/permissions`
- `doctor`
- `doctor/departments`
- `patient`
- `consult`
- `appointment`
- `prescription`
- `drug`
- `order`

`patient-h5` 当前已覆盖以下页面骨架：

- `home`
- `hospital`
- `department`
- `doctor/list`
- `doctor/detail`
- `appointment/confirm`
- `appointment/result`
- `consult/create`
- `consult/chat`
- `prescription/list`
- `order/list`
- `profile`

## 工作区结构

```text
code/frontend/
├── package.json
├── pnpm-workspace.yaml
├── README.md
├── admin-web/
└── patient-h5/
```

## 统一约束

- 管理端使用 React 18、TypeScript、Vite、Ant Design。
- 患者端使用 React 18、TypeScript、Vite、Ant Design Mobile、Zustand。
- 两个应用都需要统一支持 `satoken` 认证头透传。
- 当前阶段只搭建 MVP 骨架和页面结构，不在本文档中承诺已完成的真实联调或构建结果。

## 常用命令

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw/code/frontend
pnpm install
pnpm dev:admin
pnpm dev:patient
pnpm build
pnpm test
```

仓库根目录提供一键启停脚本，可同时管理前端和后端服务：

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

选择前端后，可继续选择启动服务、停止服务、返回上一级或退出。前端启动和停止菜单支持选择 `admin-web`、`patient-h5`、返回上一级或退出。

如需只启动前端应用：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw
SKIP_BACKEND=1 ./resources/scripts/service.sh start
```

如需只启动指定前端应用：

```bash
cd /Users/pakachuzy/Desktop/zzz/project/hlw
FRONTEND_APPS="admin-web" SKIP_BACKEND=1 ./resources/scripts/service.sh start
```

脚本默认启动：

| 应用 | 启动命令 | 本地端口 |
| --- | --- | ---: |
| `admin-web` | `pnpm dev:admin` | 3200 |
| `patient-h5` | `pnpm dev:patient` | 3300 |

脚本运行时会在仓库根目录生成 `.runtime/pids` 和 `.runtime/logs`，分别保存进程 pid 与服务日志。

## 后续维护要求

- 新增页面时同步更新对应应用的页面说明。
- 新增共享请求层、状态管理或路由约定时同步补充本文档。
- 如接入真实后端接口、鉴权流程或 WebSocket 地址，需补充环境变量和联调说明。

## 管理端接口接入

`admin-web` 的系统管理列表接口集中在 `src/api/modules.ts`，当前已接入：

- `/system/users`
- `/system/roles`
- `/system/menus`
- `/system/dicts`
- `/system/configs`
- `/system/posts`
- `/system/permissions`
- `/doctor/departments`

系统管理新增页面统一复用 `ModulePage`，样式继续收口在 `src/styles/global.css`。

科室管理页面已接入新增科室弹窗，提交后调用 `POST /doctor/departments` 并刷新列表；弹窗样式同样收口在 `src/styles/global.css`。
