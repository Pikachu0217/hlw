import axios from "axios";
import { useSessionStore } from "../store/sessionStore";

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "/api",
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = useSessionStore.getState().token;

  if (token) {
    if (typeof config.headers.set === "function") {
      config.headers.set("satoken", token);
    } else {
      config.headers = {
        ...config.headers,
        satoken: token
      };
    }
  }

  console.info("[patient-api] 发起请求", config.method?.toUpperCase(), config.url);
  return config;
});
