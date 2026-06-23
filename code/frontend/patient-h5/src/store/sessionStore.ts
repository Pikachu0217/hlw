import { create } from "zustand";

const TOKEN_KEY = "hlw:patient:token";
const VERIFIED_KEY = "hlw:patient:verified";

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

type SessionState = {
  token: string;
  patientName: string;
  phone: string;
  isLoggedIn: boolean;
  isVerified: boolean;
  setToken: (token: string) => void;
  setPatientName: (patientName: string) => void;
  setPhone: (phone: string) => void;
  setVerified: (verified: boolean) => void;
  logout: () => void;
};

export const useSessionStore = create<SessionState>((set) => ({
  token: loadToken(),
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
