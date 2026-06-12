import axios from "axios";
import { useSessionStore } from "../store/sessionStore";

export const http = axios.create({
  baseURL: "/api",
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = useSessionStore.getState().token;

  if (token) {
    config.headers.satoken = token;
  }

  return config;
});
