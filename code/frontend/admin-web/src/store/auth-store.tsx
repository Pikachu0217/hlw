import { createContext, useContext, useEffect, useMemo, useState } from 'react';
import type { PropsWithChildren } from 'react';
import {
  clearAuthSnapshot,
  type AuthSnapshot,
  persistAuthSnapshot,
  readAuthSnapshot,
} from '@/utils/auth-storage';

interface AuthContextValue extends AuthSnapshot {
  isAuthenticated: boolean;
  login: (payload: AuthSnapshot) => void;
  logout: (reason?: string) => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

/**
 * 管理端登录态上下文提供者。
 *
 * @param children 子节点
 * @return 上下文提供结果
 */
export function AuthProvider({ children }: PropsWithChildren) {
  const [snapshot, setSnapshot] = useState<AuthSnapshot>(() => readAuthSnapshot());

  /**
   * 写入登录快照。
   *
   * @param payload 登录快照
   */
  function login(payload: AuthSnapshot): void {
    console.info('[auth] 管理端登录成功', payload.displayName);
    persistAuthSnapshot(payload);
    setSnapshot(payload);
  }

  /**
   * 清理登录态。
   *
   * @param reason 退出原因
   */
  function logout(reason = '用户主动退出'): void {
    console.info('[auth] 管理端退出登录', reason);
    clearAuthSnapshot();
    setSnapshot({
      token: '',
      displayName: '医疗运营专员',
      roleName: '系统管理员',
      tenantId: 0,
    });
  }

  useEffect(() => {
    /**
     * 处理登录态失效事件。
     *
     * @param event 浏览器事件
     */
    function handleAuthExpired(event: Event): void {
      const customEvent = event as CustomEvent<{ reason?: string }>;
      logout(customEvent.detail?.reason ?? '登录态已失效');
    }

    window.addEventListener('hlw:auth-expired', handleAuthExpired);
    return () => window.removeEventListener('hlw:auth-expired', handleAuthExpired);
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      ...snapshot,
      isAuthenticated: Boolean(snapshot.token),
      login,
      logout,
    }),
    [snapshot],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

/**
 * 读取管理端登录态上下文。
 *
 * @return 登录态上下文
 */
export function useAuthStore(): AuthContextValue {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuthStore 必须在 AuthProvider 内使用');
  }

  return context;
}
