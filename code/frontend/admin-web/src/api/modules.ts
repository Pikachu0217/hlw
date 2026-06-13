import { apiClient } from '@/api/client';
import type { AppointmentRecord } from '@/pages/appointment';
import type { ConsultRecord } from '@/pages/consult';
import type { DrugRecord } from '@/pages/drug';
import type { DepartmentRecord } from '@/pages/doctor/departments';
import type { OrderRecord } from '@/pages/order';
import type { PatientRecord } from '@/pages/patient';
import type { PrescriptionRecord } from '@/pages/prescription';
import type { ConfigRecord } from '@/pages/system/configs';
import type { DictRecord } from '@/pages/system/dicts';
import type { MenuRecord } from '@/pages/system/menus';
import type { PermissionRecord } from '@/pages/system/permissions';
import type { PostRecord } from '@/pages/system/posts';
import type { RoleRecord } from '@/pages/system/roles';
import type { UserRecord } from '@/pages/system/users';
import type { TenantRecord } from '@/pages/tenant';

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface CreateDepartmentPayload {
  name: string;
  queue?: string;
  status?: string;
  sort?: number;
  description?: string;
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

// 查询模块列表并保留统一日志输出，方便前后端联调排查。
async function fetchModuleRecords<T>(url: string, moduleName: string): Promise<T[]> {
  console.info(`[admin-module] 查询${moduleName}列表`, url);
  const response = await apiClient.get<ApiResult<T[]>>(url);
  return response.data.data;
}

// 查询租户列表。
export function fetchTenants(): Promise<TenantRecord[]> {
  return fetchModuleRecords<TenantRecord>('/system/tenants', '租户');
}

// 查询后台用户列表。
export function fetchUsers(): Promise<UserRecord[]> {
  return fetchModuleRecords<UserRecord>('/system/users', '用户');
}

// 查询角色列表。
export function fetchRoles(): Promise<RoleRecord[]> {
  return fetchModuleRecords<RoleRecord>('/system/roles', '角色');
}

// 查询菜单列表。
export function fetchMenus(): Promise<MenuRecord[]> {
  return fetchModuleRecords<MenuRecord>('/system/menus', '菜单');
}

// 查询字典列表。
export function fetchDicts(): Promise<DictRecord[]> {
  return fetchModuleRecords<DictRecord>('/system/dicts', '字典');
}

// 查询系统参数配置列表。
export function fetchConfigs(): Promise<ConfigRecord[]> {
  return fetchModuleRecords<ConfigRecord>('/system/configs', '参数配置');
}

// 查询岗位列表。
export function fetchPosts(): Promise<PostRecord[]> {
  return fetchModuleRecords<PostRecord>('/system/posts', '岗位');
}

// 查询权限码列表。
export function fetchPermissions(): Promise<PermissionRecord[]> {
  return fetchModuleRecords<PermissionRecord>('/system/permissions', '权限码');
}

// 查询科室列表。
export function fetchDepartments(): Promise<DepartmentRecord[]> {
  return fetchModuleRecords<DepartmentRecord>('/doctor/departments', '科室');
}

// 创建科室。
export async function createDepartment(payload: CreateDepartmentPayload): Promise<DepartmentRecord> {
  console.info('[admin-module] 创建科室', payload);
  const response = await apiClient.post<ApiResult<DepartmentRecord>>('/doctor/departments', payload);
  return response.data.data;
}

// 查询患者列表。
export function fetchPatients(): Promise<PatientRecord[]> {
  return fetchModuleRecords<PatientRecord>('/patient/patients', '患者');
}

// 查询问诊单列表。
export function fetchConsults(): Promise<ConsultRecord[]> {
  return fetchModuleRecords<ConsultRecord>('/consult/consults', '问诊单');
}

// 创建问诊单。
export async function createConsult(payload: CreateConsultPayload): Promise<ConsultRecord> {
  console.info('[admin-module] 创建问诊单', payload);
  const response = await apiClient.post<ApiResult<ConsultRecord>>('/consult/consults', payload);
  return response.data.data;
}

// 接单问诊。
export async function acceptConsult(id: string, payload: AcceptConsultPayload): Promise<ConsultRecord> {
  console.info('[admin-module] 接单问诊', id, payload);
  const response = await apiClient.post<ApiResult<ConsultRecord>>(`/consult/consults/${id}/accept`, payload);
  return response.data.data;
}

// 完成问诊。
export async function completeConsult(id: string): Promise<ConsultRecord> {
  console.info('[admin-module] 完成问诊', id);
  const response = await apiClient.post<ApiResult<ConsultRecord>>(`/consult/consults/${id}/complete`);
  return response.data.data;
}

// 延长问诊。
export async function extendConsult(id: string): Promise<ConsultRecord> {
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
export async function payAppointment(id: string): Promise<AppointmentRecord> {
  console.info('[admin-module] 支付预约单', id);
  const response = await apiClient.post<ApiResult<AppointmentRecord>>(`/appointment/appointments/${id}/pay`);
  return response.data.data;
}

// 预约签到。
export async function checkInAppointment(id: string): Promise<AppointmentRecord> {
  console.info('[admin-module] 预约签到', id);
  const response = await apiClient.post<ApiResult<AppointmentRecord>>(`/appointment/appointments/${id}/check-in`);
  return response.data.data;
}

// 抢便民门诊预约单。
export async function grabAppointment(id: string, payload: GrabAppointmentPayload): Promise<boolean> {
  console.info('[admin-module] 抢预约单', id, payload);
  const response = await apiClient.post<ApiResult<boolean>>(`/appointment/appointments/${id}/grab`, payload);
  return response.data.data;
}

// 查询号源列表。
export function fetchNumberSources(): Promise<NumberSourceRecord[]> {
  return fetchModuleRecords<NumberSourceRecord>('/appointment/number-sources', '号源');
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
export async function submitPrescription(id: string): Promise<PrescriptionRecord> {
  console.info('[admin-module] 提交处方', id);
  const response = await apiClient.post<ApiResult<PrescriptionRecord>>(`/prescription/prescriptions/${id}/submit`);
  return response.data.data;
}

// 审核通过处方。
export async function approvePrescription(id: string, payload: ApprovePrescriptionPayload): Promise<PrescriptionRecord> {
  console.info('[admin-module] 审核通过处方', id, payload);
  const response = await apiClient.post<ApiResult<PrescriptionRecord>>(`/prescription/prescriptions/${id}/approve`, payload);
  return response.data.data;
}

// 驳回处方。
export async function rejectPrescription(id: string, payload: RejectPrescriptionPayload): Promise<PrescriptionRecord> {
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
