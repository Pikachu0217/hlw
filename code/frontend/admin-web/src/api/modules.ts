import { apiClient } from '@/api/client';
import type { AppointmentRecord } from '@/pages/appointment';
import type { ConsultRecord } from '@/pages/consult';
import type { DoctorRecord } from '@/pages/doctor/components/DoctorList';
import type { DrugRecord } from '@/pages/drug';
import type { DepartmentRecord } from '@/pages/doctor/departments';
import type { OrderRecord } from '@/pages/order';
import type { HealthRecord, PatientRecord } from '@/pages/patient';
import type { PrescriptionRecord } from '@/pages/prescription';
import type { ConfigRecord } from '@/pages/system/configs';
import type { DictRecord } from '@/pages/system/dicts';
import type { GatewayRouteRecord } from '@/pages/gateway/routes';
import type { SystemLogRecord } from '@/pages/system/logs';
import type { MenuRecord } from '@/pages/system/menus';
import type { NoticeRecord } from '@/pages/system/notices';
import type { PostRecord } from '@/pages/system/posts';
import type { RoleRecord } from '@/pages/system/roles';
import type { TenantPackageRecord } from '@/pages/system/tenant-packages';
import type { UserRecord } from '@/pages/system/users';
import type { TenantRecord } from '@/pages/tenant';

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface CreateDepartmentPayload {
  deptId?: number;
  name: string;
  queue?: string;
  status?: string;
  sort?: number;
  description?: string;
}

export interface CreateTenantPayload {
  contactUserName: string;
  contactPhone: string;
  companyName: string;
  licenseNumber?: string;
  address?: string;
  intro?: string;
  domain?: string;
  packageId?: number;
  expireTime?: string;
  accountCount?: number;
  status?: string;
  remark?: string;
}

export interface TenantOptionRecord {
  id: number;
  tenantId: string;
  companyName: string;
  status: string;
}

export interface CreateUserPayload {
  userName: string;
  realName: string;
  nickName?: string;
  phone?: string;
  email?: string;
  userType?: string;
  deptId?: number;
  sex?: string;
  status?: number;
  password?: string;
  remark?: string;
}

export interface SystemDeptRecord {
  id: number;
  parentId: number;
  deptName: string;
  ancestors: string;
  orderNum: number;
  leader?: string;
  phone?: string;
  email?: string;
  status: number;
  isDepartment?: number;
  isDefault?: number;
}

export interface CreateSystemDeptPayload {
  parentId?: number;
  deptName: string;
  orderNum?: number;
  leader?: string;
  phone?: string;
  email?: string;
  isDepartment?: number;
  status?: number;
}

export interface CreateRolePayload {
  roleName: string;
  roleCode: string;
  orderNum?: number;
  dataScope?: number;
  status?: number;
  remark?: string;
}

export interface CreateMenuPayload {
  menuName: string;
  parentId?: number;
  orderNum?: number;
  path?: string;
  component?: string;
  isFrame?: number;
  menuType?: string;
  visible?: string;
  status?: string;
  perms?: string;
  icon?: string;
  remark?: string;
}

export interface UserRoleBindingRecord {
  id: number;
  userId: string;
  userName: string;
  roleId: number;
  roleName: string;
}

export interface RoleMenuBindingRecord {
  id: number;
  roleId: number;
  roleName: string;
  menuId: number;
  menuName: string;
  perms?: string;
}

export interface CreateDictPayload {
  dictName?: string;
  dictType: string;
  dictLabel: string;
  dictValue: string;
  dictSort?: number;
  remark?: string;
}

export interface CreatePostPayload {
  postName: string;
  postCode: string;
  orderNum?: number;
  status?: number;
  remark?: string;
}

export interface CreateConfigPayload {
  configName: string;
  configKey: string;
  configValue: string;
  remark?: string;
}

export interface UpdateConfigPayload extends CreateConfigPayload {}

export interface CreateTenantPackagePayload {
  packageName: string;
  menuIds?: number[];
  status?: number;
  remark?: string;
}

export interface CreateNoticePayload {
  noticeTitle: string;
  noticeType?: string;
  noticeContent?: string;
  status?: string;
  remark?: string;
}

export interface CreateGatewayRoutePayload {
  routeCode: string;
  uri: string;
  pathPredicate: string;
  sort?: number;
  status?: string;
  remark?: string;
}

export interface CreateDoctorPayload {
  userId?: string;
  name?: string;
  title?: string;
  department?: string;
  specialty?: string;
  consultFee?: number;
  consultStatus?: string;
  status?: string;
  schedule?: string;
}

export interface CreateSchedulePayload {
  doctorId: number;
  deptId: number;
  slot: string;
  scheduleDate?: string;
  timeSlot?: string;
  totalNumber?: number;
  remainNumber?: number;
}

export interface DoctorDepartmentBindingRecord {
  id: number;
  doctorId: number;
  doctorName: string;
  deptId: number;
  departmentName: string;
  label: string;
  free?: boolean;
  appointmentFee?: number;
}

export interface BindDoctorDepartmentPayload {
  deptId: number;
  free?: boolean;
  appointmentFee?: number;
}

export interface CreateDrugPayload {
  drugName: string;
  spec: string;
  inventory?: number;
  unit?: string;
}

export interface CreateAppointmentPayload {
  patientId?: number;
  doctorId?: number;
  departmentId?: number;
  scheduleId?: number;
  patientName?: string;
  doctorName?: string;
  timeSlot?: string;
  source?: string;
  appointmentType?: string;
  feeAmount?: number;
}

export interface GrabAppointmentPayload {
  doctorId: number;
}

export interface CreateReleaseConfigPayload {
  scheduleId: number;
  releaseAt: string;
  releaseCount?: number;
  status?: string;
}

export interface NumberSourceRecord {
  id: number;
  scheduleId: number;
  numberSeq: number;
  status: string;
}

export interface DoctorConsultWorkbenchRecord {
  consultId: number;
  consultNo: string;
  patientId: number;
  patientName: string;
  doctorId: number;
  doctorName: string;
  status: string;
  channel: string;
  updatedAt: string;
  lastMessage: string;
  chiefComplaint: string;
  lastMessageTime: string;
  remainingSeconds: number;
}

export interface ConsultMessageRecord {
  id?: number;
  consultId?: number;
  senderId?: number;
  senderType?: string;
  content: string;
  contentType: 'TEXT' | 'IMAGE';
  read?: boolean;
  createTime?: string;
}

export interface CreateConsultPayload {
  patientId?: number;
  doctorId?: number;
  type?: string;
  patientName?: string;
  doctorName?: string;
  channel?: string;
  chiefComplaint?: string;
  feeAmount?: number;
}

export interface AcceptConsultPayload {
  doctorId?: number;
}

export interface CreatePrescriptionPayload {
  consultId?: number;
  patientId?: number;
  doctorId?: number;
  patientName?: string;
  doctorName?: string;
  drugIds?: number[];
  drugCount?: number;
  issuedAt?: string;
}

export interface ApprovePrescriptionPayload {
  pharmacistId?: number;
  remark?: string;
}

export interface RejectPrescriptionPayload {
  remark?: string;
}

export interface CreateOrderPayload {
  bizType?: string;
  businessType?: string;
  bizId?: number;
  patientId?: number;
  patientName?: string;
  amount?: number;
}

export interface PayOrderPayload {
  payMethod?: string;
}

export interface CreatePatientPayload {
  userId: string;
  patientName: string;
  gender: string;
  age: number;
  phone: string;
  riskLevel?: string;
  idCard?: string;
  birthday?: string;
  address?: string;
  lastVisit?: string;
}

export interface UpdatePatientPayload extends CreatePatientPayload {}

export interface CreateHealthRecordPayload {
  patientId: number;
  title: string;
  summary: string;
  allergies?: string;
  history?: string;
  diagnosis?: string;
  remark?: string;
}

// 查询模块列表并保留统一日志输出，方便前后端联调排查。
async function fetchModuleRecords<T>(url: string, moduleName: string, params?: Record<string, unknown>): Promise<T[]> {
  console.info(`[admin-module] 查询${moduleName}列表`, url, params);
  const response = await apiClient.get<ApiResult<T[]>>(url, { params });
  return response.data.data;
}

// 分页接口默认拉取首页 500 条以兼容当前“拉全量”UX，后续接入真正分页 UI 时再下调 pageSize。
// 该上限与后端 PaginationInnerInterceptor.setMaxLimit(500L) 保持一致。
interface BackendPageResult<T> {
  records: T[];
  total: number;
  pageNum: number;
  pageSize: number;
}

async function fetchModulePage<T>(
  url: string,
  moduleName: string,
  params?: { pageNum?: number; pageSize?: number; keyword?: string },
): Promise<T[]> {
  console.info(`[admin-module] 查询${moduleName}列表（分页）`, url, params);
  const response = await apiClient.get<ApiResult<BackendPageResult<T>>>(url, {
    params: { pageNum: 1, pageSize: 500, ...params },
  });
  return response.data.data.records;
}

// 查询租户列表。
export function fetchTenants(): Promise<TenantRecord[]> {
  return fetchModulePage<TenantRecord>('/system/tenant', '租户');
}

// 查询登录前可选择的租户选项。
export function fetchTenantOptions(includeDefault = false): Promise<TenantOptionRecord[]> {
  const params = includeDefault ? { includeDefault: true } : undefined;
  return fetchModuleRecords<TenantOptionRecord>('/system/tenant/options', '租户选项', params);
}

// 创建租户。
export async function createTenant(payload: CreateTenantPayload): Promise<TenantRecord> {
  console.info('[admin-module] 创建租户', payload);
  const response = await apiClient.post<ApiResult<TenantRecord>>('/system/tenant', payload);
  return response.data.data;
}

// 更新租户信息。
export async function updateTenant(id: string | number, payload: CreateTenantPayload): Promise<TenantRecord> {
  console.info('[admin-module] 更新租户', id, payload);
  const response = await apiClient.put<ApiResult<TenantRecord>>(`/system/tenant/${id}`, payload);
  return response.data.data;
}

// 删除租户。
export async function deleteTenant(id: string | number): Promise<void> {
  console.info('[admin-module] 删除租户', id);
  await apiClient.delete(`/system/tenant/${id}`);
}

// 查询后台用户列表。
export function fetchUsers(): Promise<UserRecord[]> {
  return fetchModulePage<UserRecord>('/system/user', '用户');
}

// 创建后台用户。
export async function createUser(payload: CreateUserPayload): Promise<UserRecord> {
  console.info('[admin-module] 创建用户', payload);
  const response = await apiClient.post<ApiResult<UserRecord>>('/system/user', payload);
  return response.data.data;
}

// 查询后台用户详情。
export async function fetchUserDetail(id: string | number): Promise<UserRecord> {
  console.info('[admin-module] 查询用户详情', id);
  const response = await apiClient.get<ApiResult<UserRecord>>(`/system/user/${id}`);
  return response.data.data;
}

// 更新后台用户。
export async function updateUser(id: string | number, payload: CreateUserPayload): Promise<UserRecord> {
  console.info('[admin-module] 更新用户', id, payload);
  const response = await apiClient.put<ApiResult<UserRecord>>(`/system/user/${id}`, payload);
  return response.data.data;
}

// 删除后台用户。
export async function deleteUser(id: string | number): Promise<void> {
  console.info('[admin-module] 删除用户', id);
  await apiClient.delete(`/system/user/${id}`);
}

// 查询用户角色绑定列表。
export function fetchUserRoles(): Promise<UserRoleBindingRecord[]> {
  return fetchModuleRecords<UserRoleBindingRecord>('/system/user-role', '用户角色绑定');
}

// 保存用户角色绑定，后端会先删除旧绑定再新增新绑定。
export async function bindUserRoles(userId: string, roleIds: number[]): Promise<UserRoleBindingRecord[]> {
  console.info('[admin-module] 保存用户角色绑定', userId, roleIds);
  const response = await apiClient.post<ApiResult<UserRoleBindingRecord[]>>('/system/user-role', { userId, roleIds });
  return response.data.data;
}

// 查询系统部门选项。
export function fetchSystemDeptOptions(): Promise<SystemDeptRecord[]> {
  return fetchModuleRecords<SystemDeptRecord>('/system/dept/options', '系统部门');
}

// 查询系统部门列表。
export function fetchSystemDepts(): Promise<SystemDeptRecord[]> {
  return fetchModulePage<SystemDeptRecord>('/system/dept', '系统部门');
}

// 创建系统部门。
export async function createSystemDept(payload: CreateSystemDeptPayload): Promise<SystemDeptRecord> {
  console.info('[admin-module] 创建系统部门', payload);
  const response = await apiClient.post<ApiResult<SystemDeptRecord>>('/system/dept', payload);
  return response.data.data;
}

// 查询系统部门详情。
export async function fetchSystemDeptDetail(id: string | number): Promise<SystemDeptRecord> {
  console.info('[admin-module] 查询系统部门详情', id);
  const response = await apiClient.get<ApiResult<SystemDeptRecord>>(`/system/dept/${id}`);
  return response.data.data;
}

// 更新系统部门。
export async function updateSystemDept(id: string | number, payload: CreateSystemDeptPayload): Promise<SystemDeptRecord> {
  console.info('[admin-module] 更新系统部门', id, payload);
  const response = await apiClient.put<ApiResult<SystemDeptRecord>>(`/system/dept/${id}`, payload);
  return response.data.data;
}

// 删除系统部门。
export async function deleteSystemDept(id: string | number): Promise<void> {
  console.info('[admin-module] 删除系统部门', id);
  await apiClient.delete(`/system/dept/${id}`);
}

// 查询角色列表。
export function fetchRoles(): Promise<RoleRecord[]> {
  return fetchModulePage<RoleRecord>('/system/role', '角色');
}

// 创建角色。
export async function createRole(payload: CreateRolePayload): Promise<RoleRecord> {
  console.info('[admin-module] 创建角色', payload);
  const response = await apiClient.post<ApiResult<RoleRecord>>('/system/role', payload);
  return response.data.data;
}

// 查询角色详情。
export async function fetchRoleDetail(id: string | number): Promise<RoleRecord> {
  console.info('[admin-module] 查询角色详情', id);
  const response = await apiClient.get<ApiResult<RoleRecord>>(`/system/role/${id}`);
  return response.data.data;
}

// 更新角色。
export async function updateRole(id: string | number, payload: CreateRolePayload): Promise<RoleRecord> {
  console.info('[admin-module] 更新角色', id, payload);
  const response = await apiClient.put<ApiResult<RoleRecord>>(`/system/role/${id}`, payload);
  return response.data.data;
}

// 删除角色。
export async function deleteRole(id: string | number): Promise<void> {
  console.info('[admin-module] 删除角色', id);
  await apiClient.delete(`/system/role/${id}`);
}

// 查询角色菜单绑定列表。
export function fetchRoleMenus(): Promise<RoleMenuBindingRecord[]> {
  return fetchModuleRecords<RoleMenuBindingRecord>('/system/role-menu', '角色菜单绑定');
}

// 保存角色菜单绑定，后端会先删除旧绑定再新增新绑定。
export async function bindRoleMenus(roleId: number, menuIds: number[]): Promise<RoleMenuBindingRecord[]> {
  console.info('[admin-module] 保存角色菜单绑定', roleId, menuIds);
  const response = await apiClient.post<ApiResult<RoleMenuBindingRecord[]>>('/system/role-menu', { roleId, menuIds });
  return response.data.data;
}

// 查询菜单列表。
export function fetchMenus(): Promise<MenuRecord[]> {
  return fetchModulePage<MenuRecord>('/system/menu', '菜单');
}

// 创建菜单。
export async function createMenu(payload: CreateMenuPayload): Promise<MenuRecord> {
  console.info('[admin-module] 创建菜单', payload);
  const response = await apiClient.post<ApiResult<MenuRecord>>('/system/menu', payload);
  return response.data.data;
}

// 查询菜单详情。
export async function fetchMenuDetail(id: string | number): Promise<MenuRecord> {
  console.info('[admin-module] 查询菜单详情', id);
  const response = await apiClient.get<ApiResult<MenuRecord>>(`/system/menu/${id}`);
  return response.data.data;
}

// 更新菜单。
export async function updateMenu(id: string | number, payload: CreateMenuPayload): Promise<MenuRecord> {
  console.info('[admin-module] 更新菜单', id, payload);
  const response = await apiClient.put<ApiResult<MenuRecord>>(`/system/menu/${id}`, payload);
  return response.data.data;
}

// 删除菜单。
export async function deleteMenu(id: string | number): Promise<void> {
  console.info('[admin-module] 删除菜单', id);
  await apiClient.delete(`/system/menu/${id}`);
}

// 查询字典列表。
export function fetchDicts(): Promise<DictRecord[]> {
  return fetchModulePage<DictRecord>('/system/dict', '字典');
}

// 创建字典项。
export async function createDict(payload: CreateDictPayload): Promise<DictRecord> {
  console.info('[admin-module] 创建字典项', payload);
  const response = await apiClient.post<ApiResult<DictRecord>>('/system/dict', payload);
  return response.data.data;
}

// 查询字典详情。
export async function fetchDictDetail(id: string | number): Promise<DictRecord> {
  console.info('[admin-module] 查询字典详情', id);
  const response = await apiClient.get<ApiResult<DictRecord>>(`/system/dict/${id}`);
  return response.data.data;
}

// 更新字典项。
export async function updateDict(id: string | number, payload: CreateDictPayload): Promise<DictRecord> {
  console.info('[admin-module] 更新字典项', id, payload);
  const response = await apiClient.put<ApiResult<DictRecord>>(`/system/dict/${id}`, payload);
  return response.data.data;
}

// 删除字典项。
export async function deleteDict(id: string | number): Promise<void> {
  console.info('[admin-module] 删除字典项', id);
  await apiClient.delete(`/system/dict/${id}`);
}

// 查询系统参数配置列表。
export function fetchConfigs(): Promise<ConfigRecord[]> {
  return fetchModulePage<ConfigRecord>('/system/config', '参数配置');
}

// 创建系统参数配置。
export async function createConfig(payload: CreateConfigPayload): Promise<ConfigRecord> {
  console.info('[admin-module] 创建参数配置', payload);
  const response = await apiClient.post<ApiResult<ConfigRecord>>('/system/config', payload);
  return response.data.data;
}

// 查询系统参数配置详情。
export async function fetchConfigDetail(id: string | number): Promise<ConfigRecord> {
  console.info('[admin-module] 查询参数配置详情', id);
  const response = await apiClient.get<ApiResult<ConfigRecord>>(`/system/config/${id}`);
  return response.data.data;
}

// 更新系统参数配置。
export async function updateConfig(id: string | number, payload: UpdateConfigPayload): Promise<ConfigRecord> {
  console.info('[admin-module] 更新参数配置', id, payload);
  const response = await apiClient.put<ApiResult<ConfigRecord>>(`/system/config/${id}`, payload);
  return response.data.data;
}

// 删除系统参数配置。
export async function deleteConfig(id: string | number): Promise<void> {
  console.info('[admin-module] 删除参数配置', id);
  await apiClient.delete(`/system/config/${id}`);
}

// 查询岗位列表。
export function fetchPosts(): Promise<PostRecord[]> {
  return fetchModulePage<PostRecord>('/system/post', '岗位');
}

// 创建岗位。
export async function createPost(payload: CreatePostPayload): Promise<PostRecord> {
  console.info('[admin-module] 创建岗位', payload);
  const response = await apiClient.post<ApiResult<PostRecord>>('/system/post', payload);
  return response.data.data;
}

// 查询岗位详情。
export async function fetchPostDetail(id: string | number): Promise<PostRecord> {
  console.info('[admin-module] 查询岗位详情', id);
  const response = await apiClient.get<ApiResult<PostRecord>>(`/system/post/${id}`);
  return response.data.data;
}

// 更新岗位。
export async function updatePost(id: string | number, payload: CreatePostPayload): Promise<PostRecord> {
  console.info('[admin-module] 更新岗位', id, payload);
  const response = await apiClient.put<ApiResult<PostRecord>>(`/system/post/${id}`, payload);
  return response.data.data;
}

// 删除岗位。
export async function deletePost(id: string | number): Promise<void> {
  console.info('[admin-module] 删除岗位', id);
  await apiClient.delete(`/system/post/${id}`);
}

// 查询租户套餐列表。
export function fetchTenantPackages(): Promise<TenantPackageRecord[]> {
  return fetchModulePage<TenantPackageRecord>('/system/tenant-package', '租户套餐');
}

// 创建租户套餐。
export async function createTenantPackage(payload: CreateTenantPackagePayload): Promise<TenantPackageRecord> {
  console.info('[admin-module] 创建租户套餐', payload);
  const response = await apiClient.post<ApiResult<TenantPackageRecord>>('/system/tenant-package', payload);
  return response.data.data;
}

// 查询租户套餐详情。
export async function fetchTenantPackageDetail(id: string | number): Promise<TenantPackageRecord> {
  console.info('[admin-module] 查询租户套餐详情', id);
  const response = await apiClient.get<ApiResult<TenantPackageRecord>>(`/system/tenant-package/${id}`);
  return response.data.data;
}

// 更新租户套餐。
export async function updateTenantPackage(id: string | number, payload: CreateTenantPackagePayload): Promise<TenantPackageRecord> {
  console.info('[admin-module] 更新租户套餐', id, payload);
  const response = await apiClient.put<ApiResult<TenantPackageRecord>>(`/system/tenant-package/${id}`, payload);
  return response.data.data;
}

// 删除租户套餐。
export async function deleteTenantPackage(id: string | number): Promise<void> {
  console.info('[admin-module] 删除租户套餐', id);
  await apiClient.delete(`/system/tenant-package/${id}`);
}

// 查询通知公告列表。
export function fetchNotices(): Promise<NoticeRecord[]> {
  return fetchModulePage<NoticeRecord>('/system/notice', '通知公告');
}

// 创建通知公告。
export async function createNotice(payload: CreateNoticePayload): Promise<NoticeRecord> {
  console.info('[admin-module] 创建通知公告', payload);
  const response = await apiClient.post<ApiResult<NoticeRecord>>('/system/notice', payload);
  return response.data.data;
}

// 查询通知公告详情。
export async function fetchNoticeDetail(id: string | number): Promise<NoticeRecord> {
  console.info('[admin-module] 查询通知公告详情', id);
  const response = await apiClient.get<ApiResult<NoticeRecord>>(`/system/notice/${id}`);
  return response.data.data;
}

// 更新通知公告。
export async function updateNotice(id: string | number, payload: CreateNoticePayload): Promise<NoticeRecord> {
  console.info('[admin-module] 更新通知公告', id, payload);
  const response = await apiClient.put<ApiResult<NoticeRecord>>(`/system/notice/${id}`, payload);
  return response.data.data;
}

// 删除通知公告。
export async function deleteNotice(id: string | number): Promise<void> {
  console.info('[admin-module] 删除通知公告', id);
  await apiClient.delete(`/system/notice/${id}`);
}

// 查询系统登录日志。
export function fetchSystemLoginLogs(): Promise<SystemLogRecord[]> {
  return fetchModulePage<SystemLogRecord>('/system/log/login', '系统登录日志');
}

// 查询系统操作日志。
export function fetchSystemOperatorLogs(): Promise<SystemLogRecord[]> {
  return fetchModulePage<SystemLogRecord>('/system/log/operator', '系统操作日志');
}

// 查询网关路由配置列表。
export function fetchGatewayRoutes(): Promise<GatewayRouteRecord[]> {
  return fetchModulePage<GatewayRouteRecord>('/gateway/route', '网关路由');
}

// 查询网关路由配置详情。
export async function fetchGatewayRouteDetail(id: string | number): Promise<GatewayRouteRecord> {
  console.info('[admin-module] 查询网关路由详情', id);
  const response = await apiClient.get<ApiResult<GatewayRouteRecord>>(`/gateway/route/${id}`);
  return response.data.data;
}

// 创建网关路由配置。
export async function createGatewayRoute(payload: CreateGatewayRoutePayload): Promise<GatewayRouteRecord> {
  console.info('[admin-module] 创建网关路由', payload);
  const response = await apiClient.post<ApiResult<GatewayRouteRecord>>('/gateway/route', payload);
  return response.data.data;
}

// 更新网关路由配置。
export async function updateGatewayRoute(id: string | number, payload: CreateGatewayRoutePayload): Promise<GatewayRouteRecord> {
  console.info('[admin-module] 更新网关路由', id, payload);
  const response = await apiClient.put<ApiResult<GatewayRouteRecord>>(`/gateway/route/${id}`, payload);
  return response.data.data;
}

// 删除网关路由配置。
export async function deleteGatewayRoute(id: string | number): Promise<void> {
  console.info('[admin-module] 删除网关路由', id);
  await apiClient.delete(`/gateway/route/${id}`);
}

// 查询科室列表。
export function fetchDepartments(): Promise<DepartmentRecord[]> {
  return fetchModuleRecords<DepartmentRecord>('/doctor/departments', '科室');
}

// 创建科室。
export async function createDepartment(payload: CreateDepartmentPayload): Promise<DepartmentRecord> {
  console.info('[admin-module] 开放科室资源', payload);
  const response = await apiClient.post<ApiResult<DepartmentRecord>>('/doctor/departments', payload);
  return response.data.data;
}

// 更新科室扩展属性。
export async function updateDepartmentExtension(id: string | number, payload: CreateDepartmentPayload): Promise<DepartmentRecord> {
  console.info('[admin-module] 更新科室扩展属性', id, payload);
  const response = await apiClient.put<ApiResult<DepartmentRecord>>(`/doctor/departments/${id}`, payload);
  return response.data.data;
}

// 创建医生。
export async function createDoctor(payload: CreateDoctorPayload): Promise<DoctorRecord> {
  console.info('[admin-module] 纳入医生资源', payload);
  const response = await apiClient.post<ApiResult<DoctorRecord>>('/doctor/doctors', payload);
  return response.data.data;
}

// 更新医生扩展属性。
export async function updateDoctorExtension(id: string | number, payload: CreateDoctorPayload): Promise<DoctorRecord> {
  console.info('[admin-module] 更新医生扩展属性', id, payload);
  const response = await apiClient.put<ApiResult<DoctorRecord>>(`/doctor/doctors/${id}`, payload);
  return response.data.data;
}

// 更新医生状态。
export async function updateDoctorStatus(id: string | number, status: string): Promise<unknown> {
  console.info('[admin-module] 更新医生状态', id, status);
  const response = await apiClient.put<ApiResult<unknown>>(`/doctor/doctors/${id}/status`, { status });
  return response.data.data;
}

// 查询医生科室绑定列表。
export function fetchDoctorDepartmentBindings(): Promise<DoctorDepartmentBindingRecord[]> {
  return fetchModuleRecords<DoctorDepartmentBindingRecord>('/doctor/doctor-department-bindings', '医生科室绑定');
}

// 绑定医生科室。
export async function bindDoctorDepartment(id: string | number, payload: BindDoctorDepartmentPayload): Promise<DoctorDepartmentBindingRecord> {
  console.info('[admin-module] 绑定医生科室', id, payload);
  const response = await apiClient.post<ApiResult<DoctorDepartmentBindingRecord>>(`/doctor/doctors/${id}/departments`, payload);
  return response.data.data;
}

// 创建医生排班。
export async function createDoctorSchedule(payload: CreateSchedulePayload): Promise<unknown> {
  console.info('[admin-module] 创建医生排班', payload);
  const response = await apiClient.post<ApiResult<unknown>>('/doctor/schedules', payload);
  return response.data.data;
}

export interface ScheduleRecord {
  id: number;
  doctorId: number;
  deptId: number;
  doctorName: string;
  departmentName: string;
  slot: string;
  scheduleDate: string;
  timeSlot: string;
  totalNumber: number;
  remain: number;
}

export interface ScheduleQueryParams {
  scheduleDate?: string;
  doctorId?: number;
  deptId?: number;
}

/** 查询排班列表，支持按日期/医生/科室筛选。 */
export function fetchSchedules(params?: ScheduleQueryParams): Promise<ScheduleRecord[]> {
  console.info('[admin-module] 查询排班列表', params);
  return fetchModuleRecords<ScheduleRecord>('/doctor/schedules', '排班', params as Record<string, unknown> | undefined);
}

/** 创建排班。 */
export async function createSchedule(data: Partial<ScheduleRecord> & { slot: string; totalNumber?: number }): Promise<ScheduleRecord> {
  console.info('[admin-module] 创建排班', data);
  const response = await apiClient.post<ApiResult<ScheduleRecord>>('/doctor/schedules', data);
  return response.data.data;
}

/** 更新排班。 */
export async function updateSchedule(id: number, data: Partial<ScheduleRecord> & { slot: string; totalNumber?: number }): Promise<ScheduleRecord> {
  console.info('[admin-module] 更新排班', id, data);
  const response = await apiClient.put<ApiResult<ScheduleRecord>>(`/doctor/schedules/${id}`, data);
  return response.data.data;
}

/** 删除排班。 */
export async function deleteSchedule(id: number): Promise<void> {
  console.info('[admin-module] 删除排班', id);
  await apiClient.delete<ApiResult<void>>(`/doctor/schedules/${id}`);
}

// 查询问诊单列表。
export function fetchConsults(): Promise<ConsultRecord[]> {
  return fetchModuleRecords<ConsultRecord>('/consult/consults', '问诊单');
}

// 查询当前登录医生咨询工作台。
export function fetchDoctorConsultWorkbench(): Promise<DoctorConsultWorkbenchRecord[]> {
  return fetchModuleRecords<DoctorConsultWorkbenchRecord>('/consult/doctor/workbench', '医生咨询工作台');
}

// 查询问诊消息。
export async function fetchConsultMessages(id: string | number): Promise<ConsultMessageRecord[]> {
  console.info('[admin-module] 查询问诊消息', id);
  const response = await apiClient.get<ApiResult<ConsultMessageRecord[]>>(`/consult/consults/${id}/messages`);
  return response.data.data;
}

// 创建问诊单。
export async function createConsult(payload: CreateConsultPayload): Promise<ConsultRecord> {
  console.info('[admin-module] 创建问诊单', payload);
  const response = await apiClient.post<ApiResult<ConsultRecord>>('/consult/consults', payload);
  return response.data.data;
}

// 接单问诊。
export async function acceptConsult(id: string | number, payload: AcceptConsultPayload): Promise<ConsultRecord> {
  console.info('[admin-module] 接单问诊', id, payload);
  const response = await apiClient.post<ApiResult<ConsultRecord>>(`/consult/consults/${id}/accept`, payload);
  return response.data.data;
}

// 完成问诊。
export async function completeConsult(id: string | number): Promise<ConsultRecord> {
  console.info('[admin-module] 完成问诊', id);
  const response = await apiClient.post<ApiResult<ConsultRecord>>(`/consult/consults/${id}/complete`);
  return response.data.data;
}

// 延长问诊。
export async function extendConsult(id: string | number): Promise<ConsultRecord> {
  console.info('[admin-module] 延长问诊', id);
  const response = await apiClient.post<ApiResult<ConsultRecord>>(`/consult/consults/${id}/extend`);
  return response.data.data;
}

// 查询预约单列表。
export function fetchAppointments(): Promise<AppointmentRecord[]> {
  return fetchModuleRecords<AppointmentRecord>('/appointment/appointments', '预约单');
}

// 创建预约单。
export async function createAppointment(payload: CreateAppointmentPayload): Promise<AppointmentRecord> {
  console.info('[admin-module] 创建预约单', payload);
  const response = await apiClient.post<ApiResult<AppointmentRecord>>('/appointment/appointments', payload);
  return response.data.data;
}

// 支付预约单。
export async function payAppointment(id: string | number): Promise<AppointmentRecord> {
  console.info('[admin-module] 支付预约单', id);
  const response = await apiClient.post<ApiResult<AppointmentRecord>>(`/appointment/appointments/${id}/pay`);
  return response.data.data;
}

// 预约签到。
export async function checkInAppointment(id: string | number): Promise<AppointmentRecord> {
  console.info('[admin-module] 预约签到', id);
  const response = await apiClient.post<ApiResult<AppointmentRecord>>(`/appointment/appointments/${id}/check-in`);
  return response.data.data;
}

// 抢便民门诊预约单。
export async function grabAppointment(id: string | number, payload: GrabAppointmentPayload): Promise<boolean> {
  console.info('[admin-module] 抢预约单', id, payload);
  const response = await apiClient.post<ApiResult<boolean>>(`/appointment/appointments/${id}/grab`, payload);
  return response.data.data;
}

// 查询号源列表。
export function fetchNumberSources(): Promise<NumberSourceRecord[]> {
  return fetchModuleRecords<NumberSourceRecord>('/appointment/number-sources', '号源');
}

// 号源统计信息。
export interface NumberSourceStatsRecord {
  scheduleId: number;
  totalCapacity: number;
  lockedCount: number;
  usedCount: number;
  availableCount: number;
}

// 查询号源统计信息。
export async function fetchNumberSourceStats(scheduleId: number): Promise<NumberSourceStatsRecord> {
  console.info('[admin-module] 查询号源统计信息', scheduleId);
  const response = await apiClient.get<ApiResult<NumberSourceStatsRecord>>(`/appointment/number-sources/stats/${scheduleId}`);
  return response.data.data;
}

// 锁定号源。
export async function lockNumberSource(scheduleId: number): Promise<NumberSourceRecord> {
  console.info('[admin-module] 锁定号源', scheduleId);
  const response = await apiClient.post<ApiResult<NumberSourceRecord>>(`/appointment/number-sources/${scheduleId}/lock`);
  return response.data.data;
}

// 创建放号配置。
export async function createReleaseConfig(payload: CreateReleaseConfigPayload): Promise<CreateReleaseConfigPayload & { id: number }> {
  console.info('[admin-module] 创建放号配置', payload);
  const response = await apiClient.post<ApiResult<CreateReleaseConfigPayload & { id: number }>>('/appointment/release-configs', payload);
  return response.data.data;
}

// 查询处方列表。
export function fetchPrescriptions(): Promise<PrescriptionRecord[]> {
  return fetchModuleRecords<PrescriptionRecord>('/prescription/prescriptions', '处方');
}

// 创建处方草稿。
export async function createPrescription(payload: CreatePrescriptionPayload): Promise<PrescriptionRecord> {
  console.info('[admin-module] 创建处方草稿', payload);
  const response = await apiClient.post<ApiResult<PrescriptionRecord>>('/prescription/prescriptions', payload);
  return response.data.data;
}

// 提交处方。
export async function submitPrescription(id: string | number): Promise<PrescriptionRecord> {
  console.info('[admin-module] 提交处方', id);
  const response = await apiClient.post<ApiResult<PrescriptionRecord>>(`/prescription/prescriptions/${id}/submit`);
  return response.data.data;
}

// 审核通过处方。
export async function approvePrescription(id: string | number, payload: ApprovePrescriptionPayload): Promise<PrescriptionRecord> {
  console.info('[admin-module] 审核通过处方', id, payload);
  const response = await apiClient.post<ApiResult<PrescriptionRecord>>(`/prescription/prescriptions/${id}/approve`, payload);
  return response.data.data;
}

// 驳回处方。
export async function rejectPrescription(id: string | number, payload: RejectPrescriptionPayload): Promise<PrescriptionRecord> {
  console.info('[admin-module] 驳回处方', id, payload);
  const response = await apiClient.post<ApiResult<PrescriptionRecord>>(`/prescription/prescriptions/${id}/reject`, payload);
  return response.data.data;
}

// 查询药品列表。
export function fetchDrugs(): Promise<DrugRecord[]> {
  return fetchModuleRecords<DrugRecord>('/drug/drugs', '药品');
}

// 创建药品资料。
export async function createDrug(payload: CreateDrugPayload): Promise<DrugRecord> {
  console.info('[admin-module] 创建药品', payload);
  const response = await apiClient.post<ApiResult<DrugRecord>>('/drug/drugs', payload);
  return response.data.data;
}

// 查询订单列表。
export function fetchOrders(): Promise<OrderRecord[]> {
  return fetchModuleRecords<OrderRecord>('/order/orders', '订单');
}

// 创建订单。
export async function createOrder(payload: CreateOrderPayload): Promise<OrderRecord> {
  console.info('[admin-module] 创建订单', payload);
  const response = await apiClient.post<ApiResult<OrderRecord>>('/order/orders', payload);
  return response.data.data;
}

// 支付订单。
export async function payOrder(id: string | number, payload: PayOrderPayload): Promise<OrderRecord> {
  console.info('[admin-module] 支付订单', id, payload);
  const response = await apiClient.post<ApiResult<OrderRecord>>(`/order/orders/${id}/pay`, payload);
  return response.data.data;
}

// 查询医生列表。
export async function fetchDoctors(): Promise<DoctorRecord[]> {
  return fetchModuleRecords<DoctorRecord>('/doctor/doctors', '医生');
}

// 查询患者列表。
export async function fetchPatients(): Promise<PatientRecord[]> {
  return fetchModuleRecords<PatientRecord>('/patient/patients', '患者');
}

// 查询患者详情。
export async function fetchPatientDetail(id: number): Promise<PatientRecord> {
  console.info('[admin-module] 查询患者详情', id);
  const response = await apiClient.get<ApiResult<PatientRecord>>(`/patient/patients/${id}`);
  return response.data.data;
}

// 创建患者档案。
export async function createPatient(payload: CreatePatientPayload): Promise<PatientRecord> {
  console.info('[admin-module] 创建患者档案', payload);
  const response = await apiClient.post<ApiResult<PatientRecord>>('/patient/patients', payload);
  return response.data.data;
}

// 更新患者档案。
export async function updatePatient(id: number, payload: UpdatePatientPayload): Promise<PatientRecord> {
  console.info('[admin-module] 更新患者档案', id, payload);
  const response = await apiClient.put<ApiResult<PatientRecord>>(`/patient/patients/${id}`, payload);
  return response.data.data;
}

// 查询健康档案列表。
export async function fetchHealthRecords(patientId?: number): Promise<HealthRecord[]> {
  console.info('[admin-module] 查询健康档案列表', patientId);
  const response = await apiClient.get<ApiResult<HealthRecord[]>>('/patient/health-records', {
    params: patientId ? { patientId } : undefined,
  });
  return response.data.data;
}

// 创建健康档案。
export async function createHealthRecord(payload: CreateHealthRecordPayload): Promise<HealthRecord> {
  console.info('[admin-module] 创建健康档案', payload);
  const response = await apiClient.post<ApiResult<HealthRecord>>('/patient/health-records', payload);
  return response.data.data;
}
