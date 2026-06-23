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
- `system/user`
- `system/role`
- `system/menu`
- `system/dict`
- `system/config`
- `system/post`
- `system/dept`
- `system/tenant-package`
- `system/notice`
- `system/logs`
- `gateway/routes`
- `doctor`
- `doctor/departments`
- `patient`
- `consult`
- `appointment`
- `prescription`
- `drug`
- `order`

管理端登录页已接入登录前租户选择，会调用公开租户列表接口加载可登录租户，并在提交认证时携带租户编号；登录后右上角账号区域展示真实姓名、登录账号和用户类型中文名称。

管理端 `tenant` 页面已接入租户新增、编辑和删除弹窗，字段同步当前 `sys_tenant` 企业主体模型；`system/user` 页面新增和编辑用户时需要维护真实姓名，用户类型下拉从 `user_type` 数据字典读取，登录密码支持前端生成随机密码；`doctor` 页面已接入新增医生、医生状态切换和医生排班创建操作，`doctor/departments` 页面已接入新增科室弹窗，提交后会刷新后端列表数据；`patient` 页面已接入患者档案创建、患者资料更新和健康档案创建操作，并同步展示患者详情与档案列表。

`patient-h5` 当前已覆盖以下页面骨架：

- `home`
- `hospital`
- `department`
- `doctor/list`
- `doctor/detail`
- `appointment/confirm`
- `appointment/result`
- `consult/create`
- `consult/chat`（已接入文字和图片 URL 实时问诊沟通）
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
- 两个应用都需要统一支持 `Authorization: Bearer <token>` 认证头透传。
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
| `admin-web` | `pnpm dev:admin` | 13200 |
| `patient-h5` | `pnpm dev:patient` | 13300 |

脚本运行时会在仓库根目录生成 `.runtime/pids` 和 `.runtime/logs`，分别保存进程 pid 与服务日志。

## 后续维护要求

- 新增页面时同步更新对应应用的页面说明。
- 新增共享请求层、状态管理或路由约定时同步补充本文档。
- 如接入真实后端接口、鉴权流程或 WebSocket 地址，需补充环境变量和联调说明。
- 管理端和患者端问诊 IM 首版只支持文字与图片 URL，不包含图片上传、压缩和文件鉴权。

## 管理端接口接入

`admin-web` 的系统管理列表接口集中在 `src/api/modules.ts`，侧边栏导航接口位于 `src/api/navigation.ts`，当前已接入：

- `/system/getRouters`
- `/system/user`
- `/system/role`
- `/system/menu`
- `/system/dict`
- `/system/config`
- `/system/post`
- `/system/dept`
- `/system/tenant/options`
- `/system/tenant-package`
- `/system/notice`
- `/system/log/login`
- `/system/log/operator`
- `/gateway/route`
- `/doctor/departments`

管理端登录页固定提供并默认选中平台租户 `0`，同时会在未登录状态调用 `GET /system/tenant/options` 获取业务租户编号、企业名称和状态等最小选项字段；提交 `POST /auth/login` 时会在请求体和请求头中携带所选租户编号，确保后台账号按租户隔离登录。

管理端左侧侧边栏登录后调用 `GET /system/getRouters` 获取当前账号可访问路由树，并由后端菜单配置与角色授权决定展示项；前端静态路由配置仅保留页面匹配、图标和面包屑兜底，不再作为侧边栏菜单数据源。

系统管理新增页面统一复用 `ModulePage`，样式继续收口在 `src/styles/global.css`。用户、角色、菜单、字典、参数配置、岗位、部门、租户套餐和通知公告页面均已接入新增、编辑、删除弹窗；菜单管理按 `parentId` 展示菜单树，菜单表单使用树形父级选择和内置图标选择器，列表会展示菜单图标并根据 `isDefault=0` 禁用默认菜单的编辑、删除操作，角色绑定菜单和租户套餐绑定菜单弹窗使用可勾选菜单树，租户套餐新增编辑表单也可直接勾选套餐菜单；租户和租户套餐页面属于平台级全局配置，后端要求平台租户上下文；提交后调用对应 `POST /system/*`、`PUT /system/*/{id}` 和 `DELETE /system/*/{id}` 接口并刷新列表。按钮权限统一维护在菜单 `perms` 字段，不再保留独立权限码页面。

系统日志页面已接入 `GET /system/log/login` 和 `GET /system/log/operator`，用于查看后台登录日志与操作日志；该页面只读展示，不提供写操作。

网关管理已新增 `gateway/routes` 页面，支持路由配置列表、详情、创建、编辑和删除；页面复用 `ModulePage` 和 `src/styles/global.css` 的统一样式约束。认证服务已无库化，管理端不再保留认证中心登录记录页面，登录审计统一在 `system/logs` 查看。

科室管理页面已接入新增科室弹窗，提交后调用 `POST /doctor/departments` 并刷新列表；医生管理页面已接入 `GET /doctor/doctors`、`POST /doctor/doctors`、`PUT /doctor/doctors/{id}/status` 和 `POST /doctor/schedules`，医生列表与排班弹窗继续复用统一样式；弹窗样式同样收口在 `src/styles/global.css`。

患者管理页面已接入 `GET /patient/patients`、`GET /patient/patients/{id}`、`POST /patient/patients`、`PUT /patient/patients/{id}`、`GET /patient/health-records` 和 `POST /patient/health-records`；患者资料卡片和健康档案区域继续复用 `ModulePage` 与 `src/styles/global.css` 的统一视觉约束。

药品库存页面已接入新增药品弹窗，提交后调用 `POST /drug/drugs` 并刷新列表；接口业务失败会由 `apiClient` 统一转成前端错误提示。

预约管理页面已接入预约单创建、预约支付、预约签到、便民门诊抢单、号源锁定和放号配置弹窗；预约列表与号源池均来自真实接口，页面样式统一收口在 `src/styles/global.css`。

问诊管理页面已改造为医生患者 IM 工作台，调用 `GET /consult/doctor/workbench` 展示当前登录医生名下待处理问诊患者，并通过 `/ws/consult/{consultId}` 支持文字和图片 URL 实时沟通；接单、延长和完成操作继续沿用原问诊接口，全部样式收口在 `src/styles/global.css`。

处方管理页面已接入处方草稿创建、提交审方、审核通过和驳回操作；列表和动作接口均来自后端处方服务，弹窗样式继续收口在 `src/styles/global.css`。

订单管理页面已接入订单创建和支付操作；支付弹窗支持选择支付方式，列表状态由后端订单服务返回。
