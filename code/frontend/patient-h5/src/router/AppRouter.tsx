import { Navigate, Route, Routes } from "react-router-dom";
import { AppShell } from "../components/AppShell";
import { AppointmentConfirmPage } from "../pages/appointment/confirm/AppointmentConfirmPage";
import { AppointmentListPage } from "../pages/appointment/list/AppointmentListPage";
import { AppointmentResultPage } from "../pages/appointment/result/AppointmentResultPage";
import { ConsultChatPage } from "../pages/consult/chat/ConsultChatPage";
import { ConsultCreatePage } from "../pages/consult/create/ConsultCreatePage";
import { DepartmentPage } from "../pages/department/DepartmentPage";
import { DoctorDetailPage } from "../pages/doctor/detail/DoctorDetailPage";
import { DoctorListPage } from "../pages/doctor/list/DoctorListPage";
import { HomePage } from "../pages/home/HomePage";
import { HospitalPage } from "../pages/hospital/HospitalPage";
import { OrderListPage } from "../pages/order/list/OrderListPage";
import { PrescriptionListPage } from "../pages/prescription/list/PrescriptionListPage";
import { ProfilePage } from "../pages/profile/ProfilePage";

export function AppRouter() {
  return (
    <Routes>
      <Route element={<AppShell />}>
        <Route path="/" element={<HomePage />} />
        <Route path="/hospital" element={<HospitalPage />} />
        <Route path="/department" element={<DepartmentPage />} />
        <Route path="/doctor/list" element={<DoctorListPage />} />
        <Route path="/doctor/detail" element={<DoctorDetailPage />} />
        <Route path="/appointment/confirm" element={<AppointmentConfirmPage />} />
        <Route path="/appointment/list" element={<AppointmentListPage />} />
        <Route path="/appointment/result" element={<AppointmentResultPage />} />
        <Route path="/consult/create" element={<ConsultCreatePage />} />
        <Route path="/consult/chat" element={<ConsultChatPage />} />
        <Route path="/prescription/list" element={<PrescriptionListPage />} />
        <Route path="/order/list" element={<OrderListPage />} />
        <Route path="/profile" element={<ProfilePage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
