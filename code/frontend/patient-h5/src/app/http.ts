import axios, { AxiosHeaders } from "axios";
import { useSessionStore } from "../store/sessionStore";
import { AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN_PREFIX } from "./auth-header";

export const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "/api",
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = useSessionStore.getState().token;

  if (token) {
    config.headers = AxiosHeaders.from(config.headers);
    config.headers.set(AUTHORIZATION_HEADER, `${AUTHORIZATION_TOKEN_PREFIX} ${token}`);
  }

  console.info("[patient-api] 发起请求", config.method?.toUpperCase(), config.url);
  return config;
});
