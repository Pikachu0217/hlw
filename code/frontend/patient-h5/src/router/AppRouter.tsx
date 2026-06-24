import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "../components/AppShell";
import { LoginPage } from "../pages/login/LoginPage";
import { RealNameAuthPage } from "../pages/login/RealNameAuthPage";
import { AppointmentConfirmPage } from "../pages/appointment/confirm/AppointmentConfirmPage";
import { AppointmentListPage } from "../pages/appointment/list/AppointmentListPage";
import { AppointmentResultPage } from "../pages/appointment/result/AppointmentResultPage";
import { ConsultChatPage } from "../pages/consult/chat/ConsultChatPage";
import { ConsultListPage } from "../pages/consult/list/ConsultListPage";
import { DepartmentPage } from "../pages/department/DepartmentPage";
import { DoctorDetailPage } from "../pages/doctor/detail/DoctorDetailPage";
import { DoctorListPage } from "../pages/doctor/list/DoctorListPage";
import { HomePage } from "../pages/home/HomePage";
import { HospitalPage } from "../pages/hospital/HospitalPage";
import { OrderListPage } from "../pages/order/list/OrderListPage";
import { PrescriptionListPage } from "../pages/prescription/list/PrescriptionListPage";
import { ProfilePage } from "../pages/profile/ProfilePage";
import { useSessionStore } from "../store/sessionStore";

/** 路由守卫：已登录且已实名才渲染子节点。 */
function ProtectedLayout({ children }: { children: React.ReactNode }) {
  const isLoggedIn = useSessionStore((state) => state.isLoggedIn);
  const isVerified = useSessionStore((state) => state.isVerified);

  if (!isLoggedIn) {
    return <Navigate to="/login" replace />;
  }
  if (!isVerified) {
    return <Navigate to="/real-name-auth" replace />;
  }
  return <>{children}</>;
}

/** 登录页守卫：未登录才显示，已登录则跳转。 */
function GuestLayout({ children }: { children: React.ReactNode }) {
  const isLoggedIn = useSessionStore((state) => state.isLoggedIn);
  const isVerified = useSessionStore((state) => state.isVerified);

  if (isLoggedIn && isVerified) {
    return <Navigate to="/" replace />;
  }
  if (isLoggedIn && !isVerified) {
    return <Navigate to="/real-name-auth" replace />;
  }
  return <>{children}</>;
}

/** 实名页守卫：已登录未实名才显示，否则跳转。 */
function RealNameGuard({ children }: { children: React.ReactNode }) {
  const isLoggedIn = useSessionStore((state) => state.isLoggedIn);
  const isVerified = useSessionStore((state) => state.isVerified);

  if (!isLoggedIn) {
    return <Navigate to="/login" replace />;
  }
  if (isVerified) {
    return <Navigate to="/" replace />;
  }
  return <>{children}</>;
}

export function AppRouter() {
  return (
    <Routes>
      {/* 登录页 — 未登录可访问 */}
      <Route
        path="/login"
        element={
          <GuestLayout>
            <LoginPage />
          </GuestLayout>
        }
      />

      {/* 实名认证页 — 已登录但未实名可访问 */}
      <Route
        path="/real-name-auth"
        element={
          <RealNameGuard>
            <RealNameAuthPage />
          </RealNameGuard>
        }
      />

      {/* 医院选择页 — 登录前后均可访问，用于确定患者端租户 */}
      <Route path="/hospital" element={<HospitalPage />} />

      {/* 主应用 — 已登录已实名 */}
      <Route
        path="/"
        element={
          <ProtectedLayout>
            <AppShell />
          </ProtectedLayout>
        }
      >
        <Route index element={<HomePage />} />
        <Route path="department" element={<DepartmentPage />} />
        <Route path="doctor/list" element={<DoctorListPage />} />
        <Route path="doctor/detail" element={<DoctorDetailPage />} />
        <Route path="appointment/confirm" element={<AppointmentConfirmPage />} />
        <Route path="appointment/list" element={<AppointmentListPage />} />
        <Route path="appointment/result" element={<AppointmentResultPage />} />
        <Route path="consult/list" element={<ConsultListPage />} />
        <Route path="consult/chat" element={<ConsultChatPage />} />
        <Route path="prescription/list" element={<PrescriptionListPage />} />
        <Route path="order/list" element={<OrderListPage />} />
        <Route path="profile" element={<ProfilePage />} />
      </Route>

      {/* 兜底重定向 */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
