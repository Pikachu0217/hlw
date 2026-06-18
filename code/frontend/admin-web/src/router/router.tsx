import { Navigate, createBrowserRouter } from 'react-router-dom';
import AdminLayout from '@/layouts/AdminLayout';
import RequireAuth from '@/router/RequireAuth';
import LoginPage from '@/pages/login';
import DashboardPage from '@/pages/dashboard';
import TenantPage from '@/pages/tenant';
import UsersPage from '@/pages/system/users';
import RolesPage from '@/pages/system/roles';
import MenusPage from '@/pages/system/menus';
import DictsPage from '@/pages/system/dicts';
import ConfigsPage from '@/pages/system/configs';
import PostsPage from '@/pages/system/posts';
import SystemDeptsPage from '@/pages/system/depts';
import PermissionsPage from '@/pages/system/permissions';
import DoctorPage from '@/pages/doctor';
import DepartmentsPage from '@/pages/doctor/departments';
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
      { path: 'system/user', element: <UsersPage /> },
      { path: 'system/role', element: <RolesPage /> },
      { path: 'system/menu', element: <MenusPage /> },
      { path: 'system/dict', element: <DictsPage /> },
      { path: 'system/config', element: <ConfigsPage /> },
      { path: 'system/post', element: <PostsPage /> },
      { path: 'system/dept', element: <SystemDeptsPage /> },
      { path: 'system/permission', element: <PermissionsPage /> },
      { path: 'doctor', element: <DoctorPage /> },
      { path: 'doctor/departments', element: <DepartmentsPage /> },
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
