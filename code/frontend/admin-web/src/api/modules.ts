import { apiClient } from '@/api/client';
import type { AppointmentRecord } from '@/pages/appointment';
import type { ConsultRecord } from '@/pages/consult';
import type { DrugRecord } from '@/pages/drug';
import type { OrderRecord } from '@/pages/order';
import type { PatientRecord } from '@/pages/patient';
import type { PrescriptionRecord } from '@/pages/prescription';
import type { MenuRecord } from '@/pages/system/menus';
import type { RoleRecord } from '@/pages/system/roles';
import type { UserRecord } from '@/pages/system/users';
import type { TenantRecord } from '@/pages/tenant';

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
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

// 查询患者列表。
export function fetchPatients(): Promise<PatientRecord[]> {
  return fetchModuleRecords<PatientRecord>('/patient/patients', '患者');
}

// 查询问诊单列表。
export function fetchConsults(): Promise<ConsultRecord[]> {
  return fetchModuleRecords<ConsultRecord>('/consult/consults', '问诊单');
}

// 查询预约单列表。
export function fetchAppointments(): Promise<AppointmentRecord[]> {
  return fetchModuleRecords<AppointmentRecord>('/appointment/appointments', '预约单');
}

// 查询处方列表。
export function fetchPrescriptions(): Promise<PrescriptionRecord[]> {
  return fetchModuleRecords<PrescriptionRecord>('/prescription/prescriptions', '处方');
}

// 查询药品列表。
export function fetchDrugs(): Promise<DrugRecord[]> {
  return fetchModuleRecords<DrugRecord>('/drug/drugs', '药品');
}

// 查询订单列表。
export function fetchOrders(): Promise<OrderRecord[]> {
  return fetchModuleRecords<OrderRecord>('/order/orders', '订单');
}
