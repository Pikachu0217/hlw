import { create } from "zustand";

type SessionState = {
  token: string;
  patientName: string;
  setToken: (token: string) => void;
};

export const useSessionStore = create<SessionState>((set) => ({
  token: "mock-patient-token",
  patientName: "王小雨",
  setToken: (token) => set({ token })
}));
