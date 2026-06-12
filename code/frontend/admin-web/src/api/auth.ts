import { apiClient } from '@/api/client';

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

interface LoginResponse {
  token: string;
  tenantId: number;
  userType: string;
}

export interface LoginPayload {
  username: string;
  password: string;
}

export interface AdminLoginSnapshot {
  token: string;
  displayName: string;
  roleName: string;
}

const roleNameMap: Record<string, string> = {
  ADMIN: '系统管理员',
  DOCTOR: '医生',
  PHARMACIST: '药师',
  PATIENT: '患者',
};

// 登录接口返回后端令牌，并转换成管理端鉴权快照。
export async function loginAdmin(payload: LoginPayload): Promise<AdminLoginSnapshot> {
  console.info('[auth] 请求后端登录接口', payload.username);
  const response = await apiClient.post<ApiResult<LoginResponse>>('/auth/login', payload);
  const result = response.data.data;

  return {
    token: result.token,
    displayName: payload.username,
    roleName: roleNameMap[result.userType] ?? result.userType,
  };
}
