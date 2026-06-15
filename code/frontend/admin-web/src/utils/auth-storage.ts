export interface AuthSnapshot {
  token: string;
  displayName: string;
  roleName: string;
  tenantId: number;
}

const TOKEN_KEY = 'hlw-admin-auth-token';
const PROFILE_KEY = 'hlw-admin-profile';
const TENANT_ID_KEY = 'hlw-admin-tenant-id';

/**
 * 读取当前登录快照。
 *
 * @return 管理端登录快照
 */
export function readAuthSnapshot(): AuthSnapshot {
  const token = window.localStorage.getItem(TOKEN_KEY) ?? '';
  const profileText = window.localStorage.getItem(PROFILE_KEY);
  const tenantId = Number(window.localStorage.getItem(TENANT_ID_KEY) ?? '0');

  if (!profileText) {
    return { token, displayName: '医疗运营专员', roleName: '系统管理员', tenantId };
  }

  try {
    const profile = JSON.parse(profileText) as Omit<AuthSnapshot, 'token'>;
    return {
      token,
      displayName: profile.displayName || '医疗运营专员',
      roleName: profile.roleName || '系统管理员',
      tenantId,
    };
  } catch {
    return { token, displayName: '医疗运营专员', roleName: '系统管理员', tenantId };
  }
}

/**
 * 持久化登录快照。
 *
 * @param snapshot 管理端登录快照
 */
export function persistAuthSnapshot(snapshot: AuthSnapshot): void {
  window.localStorage.setItem(TOKEN_KEY, snapshot.token);
  window.localStorage.setItem(TENANT_ID_KEY, String(snapshot.tenantId));
  window.localStorage.setItem(
    PROFILE_KEY,
    JSON.stringify({
      displayName: snapshot.displayName,
      roleName: snapshot.roleName,
    }),
  );
}

/**
 * 清理本地登录快照。
 */
export function clearAuthSnapshot(): void {
  window.localStorage.removeItem(TOKEN_KEY);
  window.localStorage.removeItem(PROFILE_KEY);
  window.localStorage.removeItem(TENANT_ID_KEY);
}

/**
 * 读取当前登录令牌。
 *
 * @return 登录令牌
 */
export function readAuthToken(): string {
  return window.localStorage.getItem(TOKEN_KEY) ?? '';
}

/**
 * 读取当前租户编号。
 *
 * @return 租户编号
 */
export function readTenantId(): number {
  return Number(window.localStorage.getItem(TENANT_ID_KEY) ?? '0');
}

/**
 * 派发登录态失效事件。
 *
 * @param reason 失效原因
 */
export function emitAuthExpiredEvent(reason: string): void {
  window.dispatchEvent(new CustomEvent('hlw:auth-expired', { detail: { reason } }));
}
