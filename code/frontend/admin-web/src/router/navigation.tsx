import type { ReactNode } from 'react';
import {
  CalendarOutlined,
  DashboardOutlined,
  DeploymentUnitOutlined,
  ExperimentOutlined,
  MedicineBoxOutlined,
  PartitionOutlined,
  SafetyCertificateOutlined,
  ShopOutlined,
  SolutionOutlined,
  TeamOutlined,
  UserSwitchOutlined,
} from '@ant-design/icons';
import { matchPath } from 'react-router-dom';

export interface NavigationItem {
  key: string;
  label: string;
  path?: string;
  subtitle?: string;
  icon?: ReactNode;
  children?: NavigationItem[];
}

export const navigationTree: NavigationItem[] = [
  { key: 'dashboard', label: '工作台', path: '/dashboard', icon: <DashboardOutlined /> },
  { key: 'tenant', label: '租户管理', path: '/tenant', icon: <ShopOutlined /> },
  {
    key: 'system',
    label: '系统管理',
    icon: <SafetyCertificateOutlined />,
    children: [
      { key: 'system-users', label: '用户管理', path: '/system/users' },
      { key: 'system-roles', label: '角色管理', path: '/system/roles' },
      { key: 'system-menus', label: '菜单管理', path: '/system/menus' },
    ],
  },
  { key: 'doctor', label: '医生管理', path: '/doctor', icon: <MedicineBoxOutlined /> },
  { key: 'patient', label: '患者管理', path: '/patient', icon: <TeamOutlined /> },
  { key: 'consult', label: '咨询单', path: '/consult', icon: <SolutionOutlined /> },
  { key: 'appointment', label: '预约管理', path: '/appointment', icon: <CalendarOutlined /> },
  { key: 'prescription', label: '处方中心', path: '/prescription', icon: <ExperimentOutlined /> },
  { key: 'drug', label: '药品目录', path: '/drug', icon: <DeploymentUnitOutlined /> },
  { key: 'order', label: '订单中心', path: '/order', icon: <PartitionOutlined /> },
  { key: 'login', label: '登录', path: '/login', icon: <UserSwitchOutlined /> },
];

export function findNavigationTrail(
  pathname: string,
  nodes: NavigationItem[] = navigationTree,
  parentTrail: NavigationItem[] = [],
): NavigationItem[] {
  for (const node of nodes) {
    const currentTrail = [...parentTrail, node];

    if (node.path && matchPath({ path: node.path, end: true }, pathname)) {
      return currentTrail;
    }

    if (node.children) {
      const childTrail = findNavigationTrail(pathname, node.children, currentTrail);
      if (childTrail.length > 0) {
        return childTrail;
      }
    }
  }

  return [];
}

export function getNavigationState(pathname: string): { selectedKeys: string[]; openKeys: string[] } {
  const trail = findNavigationTrail(pathname);
  const selectedKey = trail.length > 0 ? trail[trail.length - 1]?.key : undefined;
  const openKeys = trail.slice(0, -1).map((item) => item.key);

  return {
    selectedKeys: selectedKey ? [selectedKey] : [],
    openKeys,
  };
}
