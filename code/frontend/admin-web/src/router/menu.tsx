import {
  ApartmentOutlined,
  DashboardOutlined,
  DeploymentUnitOutlined,
  FileTextOutlined,
  MedicineBoxOutlined,
  MessageOutlined,
  ShoppingCartOutlined,
  SolutionOutlined,
  TeamOutlined,
  UserOutlined
} from "@ant-design/icons";
import type { ReactNode } from "react";

export type AppMenuItem = {
  key: string;
  label: string;
  path: string;
  icon: ReactNode;
};

export const appMenus: AppMenuItem[] = [
  { key: "dashboard", label: "运营看板", path: "/", icon: <DashboardOutlined /> },
  { key: "tenant", label: "租户管理", path: "/tenant", icon: <ApartmentOutlined /> },
  { key: "users", label: "用户管理", path: "/system/users", icon: <UserOutlined /> },
  { key: "roles", label: "角色权限", path: "/system/roles", icon: <TeamOutlined /> },
  { key: "menus", label: "菜单配置", path: "/system/menus", icon: <DeploymentUnitOutlined /> },
  { key: "doctor", label: "医生中心", path: "/doctor", icon: <MedicineBoxOutlined /> },
  { key: "patient", label: "患者档案", path: "/patient", icon: <SolutionOutlined /> },
  { key: "consult", label: "在线问诊", path: "/consult", icon: <MessageOutlined /> },
  { key: "appointment", label: "预约挂号", path: "/appointment", icon: <FileTextOutlined /> },
  { key: "prescription", label: "处方审核", path: "/prescription", icon: <FileTextOutlined /> },
  { key: "drug", label: "药品库存", path: "/drug", icon: <MedicineBoxOutlined /> },
  { key: "order", label: "订单中心", path: "/order", icon: <ShoppingCartOutlined /> }
];
