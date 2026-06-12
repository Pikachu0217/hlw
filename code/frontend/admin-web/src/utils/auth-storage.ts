export interface AuthSnapshot {
  token: string;
  displayName: string;
  roleName: string;
}

const TOKEN_KEY = 'satoken';
const PROFILE_KEY = 'hlw-admin-profile';

// 读取本地保存的登录态。
export function readAuthSnapshot(): AuthSnapshot {
  const token = window.localStorage.getItem(TOKEN_KEY) ?? '';
  const profileText = window.localStorage.getItem(PROFILE_KEY);

  if (!profileText) {
    return {
      token,
      displayName: '医疗运营专员',
      roleName: '系统管理员',
    };
  }

  try {
    const profile = JSON.parse(profileText) as Omit<AuthSnapshot, 'token'>;

    return {
      token,
      displayName: profile.displayName || '医疗运营专员',
      roleName: profile.roleName || '系统管理员',
    };
  } catch {
    return {
      token,
      displayName: '医疗运营专员',
      roleName: '系统管理员',
    };
  }
}

// 保存登录态到本地存储，便于刷新后恢复。
export function persistAuthSnapshot(snapshot: AuthSnapshot): void {
  window.localStorage.setItem(TOKEN_KEY, snapshot.token);
  window.localStorage.setItem(
    PROFILE_KEY,
    JSON.stringify({
      displayName: snapshot.displayName,
      roleName: snapshot.roleName,
    }),
  );
}

// 清理本地登录态。
export function clearAuthSnapshot(): void {
  window.localStorage.removeItem(TOKEN_KEY);
  window.localStorage.removeItem(PROFILE_KEY);
}

// 仅获取 satoken，供 API 客户端自动注入。
export function readSaToken(): string {
  return window.localStorage.getItem(TOKEN_KEY) ?? '';
}

// 广播登录失效事件，方便路由守卫与状态层同步。
export function emitAuthExpiredEvent(reason: string): void {
  window.dispatchEvent(new CustomEvent('hlw:auth-expired', { detail: { reason } }));
}
