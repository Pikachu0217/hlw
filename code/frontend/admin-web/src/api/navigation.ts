import { apiClient } from '@/api/client';

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

export interface BackendRouterMeta {
  title?: string;
  icon?: string;
}

export interface BackendRouterRecord {
  name?: string;
  path?: string;
  component?: string;
  hidden?: boolean;
  meta?: BackendRouterMeta;
  children?: BackendRouterRecord[];
}

/**
 * 查询当前登录用户可访问的后端路由树。
 *
 * @return 后端路由树
 */
export async function fetchCurrentRouters(): Promise<BackendRouterRecord[]> {
  console.info('[admin-navigation] 查询当前用户路由树');
  const response = await apiClient.get<ApiResult<BackendRouterRecord[]>>('/system/getRouters');
  return response.data.data;
}
