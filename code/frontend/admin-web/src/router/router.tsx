import { Navigate, createBrowserRouter } from 'react-router-dom';
import AdminLayout from '@/layouts/AdminLayout';
import RequireAuth from '@/router/RequireAuth';
import LoginPage from '@/pages/login';
import DashboardPage from '@/pages/dashboard';
import TenantPage from '@/pages/tenant';
import UsersPage from '@/pages/system/users';
import RolesPage from '@/pages/system/roles';
import MenusPage from '@/pages/system/menus';
import DoctorPage from '@/pages/doctor';
import PatientPage from '@/pages/patient';
import ConsultPage from '@/pages/consult';
import AppointmentPage from '@/pages/appointment';
import PrescriptionPage from '@/pages/prescription';
import DrugPage from '@/pages/drug';
import OrderPage from '@/pages/order';

export const appRouter = createBrowserRouter([
  { path: '/login', element: <LoginPage /> },
  {
    path: '/',
    element: (
      <RequireAuth>
        <AdminLayout />
      </RequireAuth>
    ),
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'tenant', element: <TenantPage /> },
      { path: 'system/users', element: <UsersPage /> },
      { path: 'system/roles', element: <RolesPage /> },
      { path: 'system/menus', element: <MenusPage /> },
      { path: 'doctor', element: <DoctorPage /> },
      { path: 'patient', element: <PatientPage /> },
      { path: 'consult', element: <ConsultPage /> },
      { path: 'appointment', element: <AppointmentPage /> },
      { path: 'prescription', element: <PrescriptionPage /> },
      { path: 'drug', element: <DrugPage /> },
      { path: 'order', element: <OrderPage /> },
    ],
  },
  { path: '*', element: <Navigate to="/dashboard" replace /> },
]);
