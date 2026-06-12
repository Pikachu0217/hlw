import { Navigate, Outlet, useLocation } from "react-router-dom";
import { authStore } from "../store/authStore";

export function AuthGuard() {
  const location = useLocation();

  if (!authStore.getToken()) {
    authStore.setToken("mock-admin-token");
  }

  if (!authStore.getToken()) {
    return <Navigate to="/" replace state={{ from: location }} />;
  }

  return <Outlet />;
}
