import { Navigate, Route, Routes } from "react-router-dom";
import { AdminLayout } from "../layouts/AdminLayout";
import { AppointmentPage } from "../pages/appointment/AppointmentPage";
import { ConsultPage } from "../pages/consult/ConsultPage";
import { DashboardPage } from "../pages/dashboard/DashboardPage";
import { DoctorPage } from "../pages/doctor/DoctorPage";
import { DrugPage } from "../pages/drug/DrugPage";
import { OrderPage } from "../pages/order/OrderPage";
import { PatientPage } from "../pages/patient/PatientPage";
import { PrescriptionPage } from "../pages/prescription/PrescriptionPage";
import { MenuPage } from "../pages/system/menus/MenuPage";
import { RolePage } from "../pages/system/roles/RolePage";
import { UserPage } from "../pages/system/users/UserPage";
import { TenantPage } from "../pages/tenant/TenantPage";
import { AuthGuard } from "./AuthGuard";

export function AppRouter() {
  return (
    <Routes>
      <Route element={<AuthGuard />}>
        <Route element={<AdminLayout />}>
          <Route path="/" element={<DashboardPage />} />
          <Route path="/tenant" element={<TenantPage />} />
          <Route path="/system/users" element={<UserPage />} />
          <Route path="/system/roles" element={<RolePage />} />
          <Route path="/system/menus" element={<MenuPage />} />
          <Route path="/doctor" element={<DoctorPage />} />
          <Route path="/patient" element={<PatientPage />} />
          <Route path="/consult" element={<ConsultPage />} />
          <Route path="/appointment" element={<AppointmentPage />} />
          <Route path="/prescription" element={<PrescriptionPage />} />
          <Route path="/drug" element={<DrugPage />} />
          <Route path="/order" element={<OrderPage />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
