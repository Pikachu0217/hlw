export interface AuthSnapshot {
  token: string;
  displayName: string;
  roleName: string;
}

const TOKEN_KEY = 'satoken';
const PROFILE_KEY = 'hlw-admin-profile';

export function readAuthSnapshot(): AuthSnapshot {
  const token = window.localStorage.getItem(TOKEN_KEY) ?? '';
  const profileText = window.localStorage.getItem(PROFILE_KEY);

  if (!profileText) {
    return { token, displayName: '医疗运营专员', roleName: '系统管理员' };
  }

  try {
    const profile = JSON.parse(profileText) as Omit<AuthSnapshot, 'token'>;
    return {
      token,
      displayName: profile.displayName || '医疗运营专员',
      roleName: profile.roleName || '系统管理员',
    };
  } catch {
    return { token, displayName: '医疗运营专员', roleName: '系统管理员' };
  }
}

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

export function clearAuthSnapshot(): void {
  window.localStorage.removeItem(TOKEN_KEY);
  window.localStorage.removeItem(PROFILE_KEY);
}

export function readSaToken(): string {
  return window.localStorage.getItem(TOKEN_KEY) ?? '';
}

export function emitAuthExpiredEvent(reason: string): void {
  window.dispatchEvent(new CustomEvent('hlw:auth-expired', { detail: { reason } }));
}
