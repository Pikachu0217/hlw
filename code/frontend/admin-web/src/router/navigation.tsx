import type { ReactNode } from 'react';
import {
  CalendarOutlined,
  CloudServerOutlined,
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
import type { BackendRouterRecord } from '@/api/navigation';

export interface NavigationItem {
  key: string;
  label: string;
  path?: string;
  subtitle?: string;
  icon?: ReactNode;
  children?: NavigationItem[];
}

const LOGIN_PATH = '/login';
const EXTERNAL_ROUTE_REGEXP = /^https?:\/\//i;
const ADMIN_NAVIGATION_REFRESH_EVENT = 'admin-navigation-refresh';

export const navigationTree: NavigationItem[] = [
  { key: 'dashboard', label: '工作台', path: '/dashboard', icon: <DashboardOutlined /> },
  { key: 'tenant', label: '租户管理', path: '/tenant', icon: <ShopOutlined /> },
  {
    key: 'system',
    label: '系统管理',
    icon: <SafetyCertificateOutlined />,
    children: [
      { key: 'system-user', label: '用户管理', path: '/system/user' },
      { key: 'system-role', label: '角色管理', path: '/system/role' },
      { key: 'system-menu', label: '菜单管理', path: '/system/menu' },
      { key: 'system-dict', label: '字典管理', path: '/system/dict' },
      { key: 'system-config', label: '参数配置', path: '/system/config' },
      { key: 'system-post', label: '岗位管理', path: '/system/post' },
      { key: 'system-dept', label: '部门管理', path: '/system/dept' },
      { key: 'system-tenant-package', label: '套餐管理', path: '/system/tenant-package' },
      { key: 'system-notice', label: '通知公告', path: '/system/notice' },
      { key: 'system-logs', label: '系统日志', path: '/system/logs' },
    ],
  },
  {
    key: 'gateway',
    label: '网关管理',
    icon: <CloudServerOutlined />,
    children: [{ key: 'gateway-routes', label: '路由配置', path: '/gateway/routes' }],
  },
  {
    key: 'doctor',
    label: '医生管理',
    icon: <MedicineBoxOutlined />,
    children: [
      { key: 'doctor-list', label: '医生名录', path: '/doctor' },
      { key: 'doctor-departments', label: '科室管理', path: '/doctor/departments' },
    ],
  },
  { key: 'patient', label: '患者管理', path: '/patient', icon: <TeamOutlined /> },
  { key: 'consult', label: '咨询单', path: '/consult', icon: <SolutionOutlined /> },
  { key: 'appointment', label: '预约管理', path: '/appointment', icon: <CalendarOutlined /> },
  { key: 'prescription', label: '处方中心', path: '/prescription', icon: <ExperimentOutlined /> },
  { key: 'drug', label: '药品目录', path: '/drug', icon: <DeploymentUnitOutlined /> },
  { key: 'order', label: '订单中心', path: '/order', icon: <PartitionOutlined /> },
  { key: 'login', label: '登录', path: '/login', icon: <UserSwitchOutlined /> },
];

const backendIconMap: Record<string, ReactNode> = {
  appointment: <CalendarOutlined />,
  calendar: <CalendarOutlined />,
  config: <SafetyCertificateOutlined />,
  dashboard: <DashboardOutlined />,
  dept: <PartitionOutlined />,
  dict: <SafetyCertificateOutlined />,
  doctor: <MedicineBoxOutlined />,
  drug: <DeploymentUnitOutlined />,
  gateway: <CloudServerOutlined />,
  menu: <SafetyCertificateOutlined />,
  order: <PartitionOutlined />,
  patient: <TeamOutlined />,
  prescription: <ExperimentOutlined />,
  role: <SafetyCertificateOutlined />,
  system: <SafetyCertificateOutlined />,
  tenant: <ShopOutlined />,
  user: <TeamOutlined />,
};

const staticNavigationByPath = new Map<string, NavigationItem>();
const staticNavigationByLabel = new Map<string, NavigationItem>();

collectStaticNavigation(navigationTree);

/**
 * 将后端路由树转换为侧边栏导航树。
 *
 * @param routers 后端路由树
 * @return 侧边栏导航树
 */
export function buildNavigationFromRouters(routers: BackendRouterRecord[]): NavigationItem[] {
  return routers
    .map((router, index) => toNavigationItem(router, String(index)))
    .filter((item): item is NavigationItem => Boolean(item));
}

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

export function getNavigationState(
  pathname: string,
  nodes: NavigationItem[] = navigationTree,
): { selectedKeys: string[]; openKeys: string[] } {
  const trail = findNavigationTrail(pathname, nodes);
  const selectedKey = trail.length > 0 ? trail[trail.length - 1]?.key : undefined;
  const openKeys = trail.slice(0, -1).map((item) => item.key);

  return {
    selectedKeys: selectedKey ? [selectedKey] : [],
    openKeys,
  };
}

/**
 * 收集静态导航配置，作为后端菜单图标和面包屑兜底。
 *
 * @param nodes 静态导航节点
 */
function collectStaticNavigation(nodes: NavigationItem[]): void {
  nodes.forEach((node) => {
    if (node.path) {
      staticNavigationByPath.set(node.path, node);
    }
    staticNavigationByLabel.set(node.label, node);

    if (node.children) {
      collectStaticNavigation(node.children);
    }
  });
}

/**
 * 转换单个后端路由节点。
 *
 * @param router 后端路由节点
 * @param fallbackKey 兜底键
 * @return 侧边栏导航节点
 */
function toNavigationItem(router: BackendRouterRecord, fallbackKey: string): NavigationItem | null {
  if (router.hidden) {
    return null;
  }

  const path = normalizeRoutePath(router.path);
  const label = router.meta?.title?.trim() || router.name?.trim() || path || fallbackKey;
  const children = router.children
    ?.map((child, index) => toNavigationItem(child, `${fallbackKey}-${index}`))
    .filter((item): item is NavigationItem => Boolean(item));

  if (!path && (!children || children.length === 0)) {
    return null;
  }

  return {
    key: buildNavigationKey(label, path, fallbackKey),
    label,
    path: children?.length ? undefined : path,
    icon: resolveNavigationIcon(router, label, path),
    children: children?.length ? children : undefined,
  };
}

/**
 * 标准化后端路由路径。
 *
 * @param path 后端路由路径
 * @return 前端可跳转路径
 */
function normalizeRoutePath(path?: string): string | undefined {
  const trimmedPath = path?.trim();

  if (!trimmedPath) {
    return undefined;
  }

  if (EXTERNAL_ROUTE_REGEXP.test(trimmedPath)) {
    return trimmedPath;
  }

  return trimmedPath.startsWith('/') ? trimmedPath : `/${trimmedPath}`;
}

/**
 * 构造菜单节点稳定键。
 *
 * @param label 菜单标题
 * @param path 菜单路径
 * @param fallbackKey 兜底键
 * @return 菜单节点键
 */
function buildNavigationKey(label: string, path: string | undefined, fallbackKey: string): string {
  return path ?? `${label}-${fallbackKey}`;
}

/**
 * 解析后端菜单图标。
 *
 * @param router 后端路由节点
 * @param label 菜单标题
 * @param path 菜单路径
 * @return 菜单图标
 */
function resolveNavigationIcon(router: BackendRouterRecord, label: string, path?: string): ReactNode {
  const iconKey = router.meta?.icon?.trim().toLowerCase();

  if (iconKey && backendIconMap[iconKey]) {
    return backendIconMap[iconKey];
  }

  if (path && staticNavigationByPath.get(path)?.icon) {
    return staticNavigationByPath.get(path)?.icon;
  }

  return staticNavigationByLabel.get(label)?.icon;
}

export { ADMIN_NAVIGATION_REFRESH_EVENT, LOGIN_PATH };
