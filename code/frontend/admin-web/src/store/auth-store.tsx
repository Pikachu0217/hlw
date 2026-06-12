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

export function AuthProvider({ children }: PropsWithChildren) {
  const [snapshot, setSnapshot] = useState<AuthSnapshot>(() => readAuthSnapshot());

  function login(payload: AuthSnapshot): void {
    console.info('[auth] 管理端登录成功', payload.displayName);
    persistAuthSnapshot(payload);
    setSnapshot(payload);
  }

  function logout(reason = '用户主动退出'): void {
    console.info('[auth] 管理端退出登录', reason);
    clearAuthSnapshot();
    setSnapshot({
      token: '',
      displayName: '医疗运营专员',
      roleName: '系统管理员',
    });
  }

  useEffect(() => {
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

export function useAuthStore(): AuthContextValue {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error('useAuthStore 必须在 AuthProvider 内使用');
  }

  return context;
}
