import { create } from "zustand";

type SessionState = {
  token: string;
  patientName: string;
  setToken: (token: string) => void;
  setPatientName: (patientName: string) => void;
};

export const useSessionStore = create<SessionState>((set) => ({
  token: "demo-token-2-100",
  patientName: "王小雨",
  setToken: (token) => set({ token }),
  setPatientName: (patientName) => set({ patientName })
}));
