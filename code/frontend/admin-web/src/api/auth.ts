import { apiClient } from '@/api/client';

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

interface LoginResponse {
  token: string;
  tenantId: string | number;
  userType: string;
}

export interface LoginPayload {
  tenantId: string;
  username: string;
  password: string;
}

export interface AdminLoginSnapshot {
  token: string;
  displayName: string;
  roleName: string;
  tenantId: string;
}

const roleNameMap: Record<string, string> = {
  ADMIN: '系统管理员',
  DOCTOR: '医生',
  PHARMACIST: '药师',
  PATIENT: '患者',
};

/**
 * 调用后台登录接口并转换管理端登录快照。
 *
 * @param payload 登录参数
 * @return 登录快照
 */
export async function loginAdmin(payload: LoginPayload): Promise<AdminLoginSnapshot> {
  console.info('[auth] 请求后端登录接口', payload.username, payload.tenantId);
  const response = await apiClient.post<ApiResult<LoginResponse>>('/auth/login', payload, {
    headers: { 'X-Tenant-Id': payload.tenantId },
  });
  const result = response.data.data;

  return {
    token: result.token,
    displayName: payload.username,
    roleName: roleNameMap[result.userType] ?? result.userType,
    tenantId: payload.tenantId || String(result.tenantId),
  };
}
