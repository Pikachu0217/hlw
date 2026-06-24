import { create } from "zustand";

const TOKEN_KEY = "hlw:patient:token";
const VERIFIED_KEY = "hlw:patient:verified";
const TENANT_ID_KEY = "hlw:patient:tenant-id";
const TENANT_NAME_KEY = "hlw:patient:tenant-name";

function loadToken(): string {
  try {
    return localStorage.getItem(TOKEN_KEY) ?? "";
  } catch {
    return "";
  }
}

function loadVerified(): boolean {
  try {
    return localStorage.getItem(VERIFIED_KEY) === "true";
  } catch {
    return false;
  }
}

function loadTenantId(): string {
  try {
    return localStorage.getItem(TENANT_ID_KEY) ?? "100";
  } catch {
    return "100";
  }
}

function loadTenantName(): string {
  try {
    return localStorage.getItem(TENANT_NAME_KEY) ?? "";
  } catch {
    return "";
  }
}

type SessionState = {
  token: string;
  tenantId: string;
  tenantName: string;
  patientName: string;
  phone: string;
  isLoggedIn: boolean;
  isVerified: boolean;
  setToken: (token: string) => void;
  setTenant: (tenantId: string, tenantName: string) => void;
  setPatientName: (patientName: string) => void;
  setPhone: (phone: string) => void;
  setVerified: (verified: boolean) => void;
  logout: () => void;
};

export const useSessionStore = create<SessionState>((set) => ({
  token: loadToken(),
  tenantId: loadTenantId(),
  tenantName: loadTenantName(),
  patientName: "",
  phone: "",
  isLoggedIn: !!loadToken(),
  isVerified: loadVerified(),
  setToken: (token) => {
    try {
      localStorage.setItem(TOKEN_KEY, token);
    } catch { /* 无痕模式降级 */ }
    set({ token, isLoggedIn: !!token });
  },
  setTenant: (tenantId, tenantName) => {
    try {
      localStorage.setItem(TENANT_ID_KEY, tenantId);
      localStorage.setItem(TENANT_NAME_KEY, tenantName);
    } catch { /* 无痕模式降级 */ }
    set({ tenantId, tenantName });
  },
  setPatientName: (patientName) => set({ patientName }),
  setPhone: (phone) => set({ phone }),
  setVerified: (verified) => {
    try {
      localStorage.setItem(VERIFIED_KEY, verified ? "true" : "false");
    } catch { /* 无痕模式降级 */ }
    set({ isVerified: verified });
  },
  logout: () => {
    try {
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(VERIFIED_KEY);
    } catch { /* 无痕模式降级 */ }
    set({ token: "", isLoggedIn: false, isVerified: false, patientName: "", phone: "" });
  }
}));
