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

// 定义管理端导航树，供路由高亮与侧边菜单复用。
export const navigationTree: NavigationItem[] = [
  {
    key: 'dashboard',
    label: '工作台',
    path: '/dashboard',
    subtitle: '总览今日门诊、咨询与履约进展。',
    icon: <DashboardOutlined />,
  },
  {
    key: 'tenant',
    label: '租户管理',
    path: '/tenant',
    subtitle: '统一查看租户套餐、管理员与到期风险。',
    icon: <ShopOutlined />,
  },
  {
    key: 'system',
    label: '系统管理',
    icon: <SafetyCertificateOutlined />,
    children: [
      {
        key: 'system-users',
        label: '用户管理',
        path: '/system/users',
        subtitle: '维护后台用户、科室归属与最近登录情况。',
      },
      {
        key: 'system-roles',
        label: '角色管理',
        path: '/system/roles',
        subtitle: '梳理权限角色、数据范围与启停状态。',
      },
      {
        key: 'system-menus',
        label: '菜单管理',
        path: '/system/menus',
        subtitle: '配置导航结构、权限标识与路由入口。',
      },
    ],
  },
  {
    key: 'doctor',
    label: '医生管理',
    path: '/doctor',
    subtitle: '集中管理医生名录、排班与接诊状态。',
    icon: <MedicineBoxOutlined />,
  },
  {
    key: 'patient',
    label: '患者管理',
    path: '/patient',
    subtitle: '管理患者档案、风险等级与最近就诊记录。',
    icon: <TeamOutlined />,
  },
  {
    key: 'consult',
    label: '咨询单',
    path: '/consult',
    subtitle: '跟进图文、电话与视频咨询全流程。',
    icon: <SolutionOutlined />,
  },
  {
    key: 'appointment',
    label: '预约管理',
    path: '/appointment',
    subtitle: '掌握挂号来源、门诊时段与到诊执行。',
    icon: <CalendarOutlined />,
  },
  {
    key: 'prescription',
    label: '处方中心',
    path: '/prescription',
    subtitle: '查看处方流转、审方状态与发药准备。',
    icon: <ExperimentOutlined />,
  },
  {
    key: 'drug',
    label: '药品目录',
    path: '/drug',
    subtitle: '维护药品库存、规格与预警标签。',
    icon: <DeploymentUnitOutlined />,
  },
  {
    key: 'order',
    label: '订单中心',
    path: '/order',
    subtitle: '核对订单金额、支付状态与业务类型。',
    icon: <PartitionOutlined />,
  },
  {
    key: 'login',
    label: '登录',
    path: '/login',
    subtitle: '登录页',
    icon: <UserSwitchOutlined />,
  },
];

// 根据当前路径定位命中的导航链路。
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

// 返回当前菜单的选中项与展开项。
export function getNavigationState(pathname: string): { selectedKeys: string[]; openKeys: string[] } {
  const trail = findNavigationTrail(pathname);
  const selectedKey = trail.at(-1)?.key;
  const openKeys = trail.slice(0, -1).map((item) => item.key);

  return {
    selectedKeys: selectedKey ? [selectedKey] : [],
    openKeys,
  };
}
