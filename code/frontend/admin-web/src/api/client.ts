import axios, { AxiosError } from 'axios';
import {
  clearAuthSnapshot,
  emitAuthExpiredEvent,
  readSaToken,
} from '@/utils/auth-storage';

// 创建统一的 API 客户端实例。
export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 15000,
});

// 请求拦截器：自动携带 satoken。
apiClient.interceptors.request.use((config) => {
  const token = readSaToken();

  if (token) {
    if (typeof config.headers.set === 'function') {
      config.headers.set('satoken', token);
    } else {
      config.headers = {
        ...config.headers,
        satoken: token,
      };
    }
  }

  console.info('[api] 发起请求', config.method?.toUpperCase(), config.url);
  return config;
});

// 响应拦截器：统一处理登录过期场景。
apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      console.warn('[api] satoken 已失效，准备跳转登录页');
      clearAuthSnapshot();
      emitAuthExpiredEvent('接口返回 401，登录态已失效');

      if (window.location.pathname !== '/login') {
        window.location.replace('/login');
      }
    }

    return Promise.reject(error);
  },
);

// 透出带类型推断的 GET 请求方法，便于后续模块复用。
export async function getRequest<T>(url: string, params?: Record<string, unknown>): Promise<T> {
  const response = await apiClient.get<T>(url, { params });
  return response.data;
}
