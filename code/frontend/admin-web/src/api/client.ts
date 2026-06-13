import axios, { AxiosError, AxiosHeaders } from 'axios';
import {
  clearAuthSnapshot,
  emitAuthExpiredEvent,
  readSaToken,
} from '@/utils/auth-storage';

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? '/api',
  timeout: 15000,
});

apiClient.interceptors.request.use((config) => {
  const token = readSaToken();

  if (token) {
    config.headers = AxiosHeaders.from(config.headers);
    config.headers.set('satoken', token);
  }

  console.info('[api] 发起请求', config.method?.toUpperCase(), config.url);
  return config;
});

apiClient.interceptors.response.use(
  (response) => {
    const payload = response.data as { code?: number; message?: string } | undefined;

    if (payload?.code && payload.code !== 200) {
      console.warn('[api] 业务请求失败', payload.code, payload.message);
      return Promise.reject(new Error(payload.message ?? '接口业务处理失败'));
    }

    return response;
  },
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
