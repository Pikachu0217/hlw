import axios from "axios";
import { authStore } from "../store/authStore";

export const http = axios.create({
  baseURL: "/api",
  timeout: 10000
});

http.interceptors.request.use((config) => {
  const token = authStore.getToken();

  if (token) {
    config.headers.satoken = token;
  }

  return config;
});
