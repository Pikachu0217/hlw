export interface AuthSnapshot {
  token: string;
  displayName: string;
  username: string;
  userType: string;
  roleName: string;
  tenantId: string;
}

const TOKEN_KEY = 'hlw-admin-auth-token';
const PROFILE_KEY = 'hlw-admin-profile';
const TENANT_ID_KEY = 'hlw-admin-tenant-id';
const DEFAULT_DISPLAY_NAME = '医疗运营专员';
const DEFAULT_USERNAME = 'hlw_admin';
const DEFAULT_USER_TYPE = 'sys_user';
const DEFAULT_USER_TYPE_NAME = '系统用户';
const DEFAULT_TENANT_ID = '0';

/**
 * 生成默认登录快照。
 *
 * @param token 登录令牌
 * @param tenantId 租户编号
 * @return 默认登录快照
 */
function defaultAuthSnapshot(token = '', tenantId = DEFAULT_TENANT_ID): AuthSnapshot {
  return {
    token,
    displayName: DEFAULT_DISPLAY_NAME,
    username: DEFAULT_USERNAME,
    userType: DEFAULT_USER_TYPE,
    roleName: DEFAULT_USER_TYPE_NAME,
    tenantId,
  };
}

/**
 * 读取当前登录快照。
 *
 * @return 管理端登录快照
 */
export function readAuthSnapshot(): AuthSnapshot {
  const token = window.localStorage.getItem(TOKEN_KEY) ?? '';
  const profileText = window.localStorage.getItem(PROFILE_KEY);
  const tenantId = window.localStorage.getItem(TENANT_ID_KEY) ?? DEFAULT_TENANT_ID;

  if (!profileText) {
    return defaultAuthSnapshot(token, tenantId);
  }

  try {
    const profile = JSON.parse(profileText) as Omit<AuthSnapshot, 'token'>;
    return {
      token,
      displayName: profile.displayName || DEFAULT_DISPLAY_NAME,
      username: profile.username || DEFAULT_USERNAME,
      userType: profile.userType || DEFAULT_USER_TYPE,
      roleName: profile.roleName || DEFAULT_USER_TYPE_NAME,
      tenantId,
    };
  } catch {
    return defaultAuthSnapshot(token, tenantId);
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
      username: snapshot.username,
      userType: snapshot.userType,
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
export function readTenantId(): string {
  return window.localStorage.getItem(TENANT_ID_KEY) ?? DEFAULT_TENANT_ID;
}

/**
 * 派发登录态失效事件。
 *
 * @param reason 失效原因
 */
export function emitAuthExpiredEvent(reason: string): void {
  window.dispatchEvent(new CustomEvent('hlw:auth-expired', { detail: { reason } }));
}
