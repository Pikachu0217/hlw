import {
  BellOutlined,
  DownOutlined,
  LogoutOutlined,
  MenuOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Avatar, Badge, Breadcrumb, Button, Drawer, Dropdown, Layout, Menu, Space, Tag, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { useEffect, useMemo, useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { fetchDicts } from '@/api/modules';
import { fetchCurrentRouters } from '@/api/navigation';
import {
  ADMIN_NAVIGATION_REFRESH_EVENT,
  LOGIN_PATH,
  buildNavigationFromRouters,
  findNavigationTrail,
  getNavigationState,
  navigationTree,
  type NavigationItem,
} from '@/router/navigation';
import { useAuthStore } from '@/store/auth-store';

const { Header, Content, Sider } = Layout;
const ADMIN_SIDER_WIDTH = 264;
const MOBILE_MENU_DRAWER_WIDTH = 292;
const USER_TYPE_DICT_TYPE = 'user_type';

type MenuItem = Required<MenuProps>['items'][number];

function buildMenuItems(items = navigationTree): MenuItem[] {
  return items
    .filter((item) => item.path !== LOGIN_PATH)
    .map((item) => {
      if (item.children?.length) {
        return {
          key: item.key,
          icon: item.icon,
          label: item.label,
          children: buildMenuItems(item.children),
        };
      }

      return {
        key: item.key,
        icon: item.icon,
        label: item.label,
      };
    });
}

function findPathByKey(key: string, items = navigationTree): string | undefined {
  for (const item of items) {
    if (item.key === key) {
      return item.path;
    }

    if (item.children) {
      const path = findPathByKey(key, item.children);

      if (path) {
        return path;
      }
    }
  }

  return undefined;
}

function buildBreadcrumbItems(pathname: string, items: NavigationItem[]): { title: string }[] {
  const breadcrumbItems = findNavigationTrail(pathname, items).length > 0 ? items : navigationTree;
  const { selectedKeys, openKeys } = getNavigationState(pathname, breadcrumbItems);
  const keys = [...openKeys, ...selectedKeys];
  const titleMap = new Map<string, string>();

  function collectTitles(nodes: NavigationItem[] = breadcrumbItems): void {
    nodes.forEach((item) => {
      titleMap.set(item.key, item.label);

      if (item.children) {
        collectTitles(item.children);
      }
    });
  }

  collectTitles();
  return keys.map((key) => ({ title: titleMap.get(key) ?? key }));
}

function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { displayName, username, userType, roleName, logout } = useAuthStore();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [navigationItems, setNavigationItems] = useState<NavigationItem[]>([]);
  const [menuLoading, setMenuLoading] = useState(true);
  const [userTypeName, setUserTypeName] = useState(roleName);
  const navigationState = getNavigationState(location.pathname, navigationItems);
  const [openKeys, setOpenKeys] = useState<string[]>([]);
  const navigationOpenKeySignature = navigationState.openKeys.join('|');
  const breadcrumbItems = buildBreadcrumbItems(location.pathname, navigationItems);
  const menuItems = useMemo(() => buildMenuItems(navigationItems), [navigationItems]);
  const accountInitial = displayName.trim().slice(0, 1).toUpperCase() || 'H';
  const resolvedUserTypeName = userTypeName || roleName || userType;

  useEffect(() => {
    let active = true;

    /** 加载当前登录用户可访问的后端菜单。 */
    async function loadNavigationItems(): Promise<void> {
      setMenuLoading(true);
      try {
        const routers = await fetchCurrentRouters();
        const nextNavigationItems = buildNavigationFromRouters(routers);
        if (active) {
          setNavigationItems(nextNavigationItems);
        }
      } catch (error) {
        console.warn('[admin-navigation] 后端菜单加载失败', error);
        if (active) {
          setNavigationItems([]);
        }
      } finally {
        if (active) {
          setMenuLoading(false);
        }
      }
    }

    /** 响应菜单配置变更事件，重新加载后端菜单。 */
    function handleNavigationRefresh(): void {
      void loadNavigationItems();
    }

    void loadNavigationItems();
    window.addEventListener(ADMIN_NAVIGATION_REFRESH_EVENT, handleNavigationRefresh);

    return () => {
      active = false;
      window.removeEventListener(ADMIN_NAVIGATION_REFRESH_EVENT, handleNavigationRefresh);
    };
  }, []);

  useEffect(() => {
    let active = true;

    /** 加载用户类型字典，用于右上角展示中文用户类型。 */
    async function loadUserTypeName(): Promise<void> {
      try {
        const dicts = await fetchDicts();
        const matchedDict = dicts.find((dict) => dict.dictType === USER_TYPE_DICT_TYPE && dict.dictValue === userType);
        if (active) {
          setUserTypeName(matchedDict?.dictLabel ?? roleName ?? userType);
        }
      } catch (error) {
        console.warn('[admin-layout] 用户类型字典加载失败', error);
        if (active) {
          setUserTypeName(roleName ?? userType);
        }
      }
    }

    void loadUserTypeName();
    return () => {
      active = false;
    };
  }, [roleName, userType]);

  useEffect(() => {
    setOpenKeys(navigationState.openKeys);
  }, [navigationOpenKeySignature]);

  /** 处理导航菜单点击，并在移动端收起抽屉。 */
  function handleMenuClick({ key }: { key: string }): void {
    const path = findPathByKey(key, navigationItems);

    if (path) {
      if (path.startsWith('http')) {
        window.open(path, '_blank', 'noopener,noreferrer');
        return;
      }

      navigate(path);
      setMobileMenuOpen(false);
    }
  }

  /** 处理右上角账号菜单点击。 */
  function handleUserMenuClick({ key }: { key: string }): void {
    if (key === 'logout') {
      logout('用户从右上角菜单退出');
      navigate('/login', { replace: true });
    }
  }

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '当前账号',
      disabled: true,
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
    },
  ];

  /** 渲染桌面侧边栏与移动端抽屉共用的导航内容。 */
  function renderNavigationContent(): JSX.Element {
    return (
      <>
        <div className="brand-panel">
          <span className="brand-chip">HLW Cloud</span>
          <Typography.Title level={3} className="brand-title">
            互联网医院管理台
          </Typography.Title>
          <Typography.Paragraph className="brand-copy">
            统一管理租户、医生、问诊、预约、处方、药品与订单。
          </Typography.Paragraph>
        </div>
        <Menu
          className="admin-menu"
          mode="inline"
          theme="dark"
          items={menuItems}
          selectedKeys={navigationState.selectedKeys}
          openKeys={openKeys}
          onOpenChange={(keys) => setOpenKeys(keys.map(String))}
          onClick={handleMenuClick}
        />
        {!menuLoading && menuItems.length === 0 ? <div className="admin-menu-status">暂无可访问菜单</div> : null}
        {menuLoading ? <div className="admin-menu-status">菜单加载中</div> : null}
      </>
    );
  }

  return (
    <Layout className="admin-shell">
      <Sider width={ADMIN_SIDER_WIDTH} className="admin-sider">
        {renderNavigationContent()}
      </Sider>
      <Layout>
        <Header className="admin-header">
          <div className="header-main">
            <Button
              className="admin-mobile-menu-button"
              shape="circle"
              icon={<MenuOutlined />}
              aria-label="打开导航菜单"
              onClick={() => setMobileMenuOpen(true)}
            />
            <div>
              <Typography.Text className="header-label">租户视角</Typography.Text>
              <Breadcrumb items={breadcrumbItems} className="admin-breadcrumb" />
              <div className="header-title-row">
                <Typography.Title level={4} className="header-title">
                  华林云互联网医院 SaaS
                </Typography.Title>
                <Tag color="cyan">MVP 骨架</Tag>
              </div>
            </div>
          </div>
          <Space size="middle" className="header-actions">
            <Badge dot>
              <Button className="header-notice" shape="circle" icon={<BellOutlined />} />
            </Badge>
            <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }} trigger={['click']}>
              <button className="header-user" type="button">
                <span className="header-user__avatar-wrap">
                  <Avatar className="theme-avatar">{accountInitial}</Avatar>
                  <span className="header-user__status" />
                </span>
                <div className="header-user__meta">
                  <Typography.Text className="header-user__name" strong>
                    {displayName}
                  </Typography.Text>
                  <span className="header-user__details">
                    <span className="header-user__username">{username}</span>
                    <span className="header-user__type">{resolvedUserTypeName}</span>
                  </span>
                </div>
                <DownOutlined className="header-user__arrow" />
              </button>
            </Dropdown>
          </Space>
        </Header>
        <Content className="admin-content">
          <Outlet />
        </Content>
      </Layout>
      <Drawer
        className="admin-mobile-drawer"
        width={MOBILE_MENU_DRAWER_WIDTH}
        placement="left"
        open={mobileMenuOpen}
        onClose={() => setMobileMenuOpen(false)}
        closable={false}
      >
        {renderNavigationContent()}
      </Drawer>
    </Layout>
  );
}

export default AdminLayout;
