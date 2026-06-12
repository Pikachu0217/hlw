import {
  BellOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Avatar, Breadcrumb, Button, Drawer, Dropdown, Layout, Menu, Space, Tag, Typography } from 'antd';
import type { MenuProps } from 'antd';
import { useEffect, useState } from 'react';
import { Outlet, useLocation, useNavigate } from 'react-router-dom';
import { getNavigationState, navigationTree } from '@/router/navigation';
import { useAuthStore } from '@/store/auth-store';

const { Header, Sider, Content } = Layout;

type MenuItem = Required<MenuProps>['items'][number];

// 将导航树转换为 Ant Design Menu 所需的数据结构。
function buildMenuItems(items = navigationTree): MenuItem[] {
  return items
    .filter((item) => item.path !== '/login')
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

// 根据菜单 key 反查目标路径。
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

// 根据当前路径构建面包屑数据。
function buildBreadcrumbItems(pathname: string): { title: string }[] {
  const { selectedKeys, openKeys } = getNavigationState(pathname);
  const keys = [...openKeys, ...selectedKeys];
  const titleMap = new Map<string, string>();

  function collectTitles(items = navigationTree): void {
    items.forEach((item) => {
      titleMap.set(item.key, item.label);

      if (item.children) {
        collectTitles(item.children);
      }
    });
  }

  collectTitles();
  return keys.map((key) => ({ title: titleMap.get(key) ?? key }));
}

// 提供管理端主布局、导航和顶部操作区。
function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const { displayName, roleName, logout } = useAuthStore();
  const navigationState = getNavigationState(location.pathname);
  const [collapsed, setCollapsed] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [openKeys, setOpenKeys] = useState<string[]>(navigationState.openKeys);
  const menuItems = buildMenuItems();
  const breadcrumbItems = buildBreadcrumbItems(location.pathname);

  // 路由变化时同步展开状态，避免菜单与页面不一致。
  useEffect(() => {
    setOpenKeys(navigationState.openKeys);
  }, [location.pathname]);

  // 响应菜单点击并切换路由。
  function handleMenuClick({ key }: { key: string }): void {
    const path = findPathByKey(key);

    if (path) {
      setMobileMenuOpen(false);
      navigate(path);
    }
  }

  // 处理右上角用户菜单动作。
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

  return (
    <Layout className="admin-layout">
      <Sider
        collapsible
        collapsed={collapsed}
        trigger={null}
        width={268}
        className="admin-sider"
      >
        <div className="brand-panel">
          <span className="brand-panel__mark">HLW</span>
          {!collapsed ? (
            <div>
              <strong className="brand-panel__title">医疗控制台</strong>
              <span className="brand-panel__subtitle">明亮、稳态、可扩展</span>
            </div>
          ) : null}
        </div>
        <Menu
          mode="inline"
          theme="dark"
          items={menuItems}
          selectedKeys={navigationState.selectedKeys}
          openKeys={collapsed ? [] : openKeys}
          onOpenChange={(keys) => setOpenKeys(keys as string[])}
          onClick={handleMenuClick}
          className="admin-menu"
        />
      </Sider>
      <Layout className="admin-main">
        <Header className="admin-header">
          <Space size="middle">
            <Button
              type="text"
              className="header-trigger"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed((value) => !value)}
            />
            <Button
              type="text"
              className="header-trigger header-trigger--mobile"
              icon={<MenuUnfoldOutlined />}
              onClick={() => setMobileMenuOpen(true)}
            />
            <div>
              <Breadcrumb items={breadcrumbItems} className="admin-breadcrumb" />
              <Typography.Title level={4} className="admin-header__title">
                {breadcrumbItems.at(-1)?.title ?? '医疗管理台'}
              </Typography.Title>
            </div>
          </Space>
          <Space size="middle">
            <Tag color="cyan" className="header-tag">
              satoken 已接入
            </Tag>
            <Button shape="circle" icon={<BellOutlined />} />
            <Dropdown menu={{ items: userMenuItems, onClick: handleUserMenuClick }} trigger={['click']}>
              <Space className="header-user">
                <Avatar className="header-user__avatar">{displayName.slice(0, 1)}</Avatar>
                <div className="header-user__meta">
                  <strong>{displayName}</strong>
                  <span>{roleName}</span>
                </div>
              </Space>
            </Dropdown>
          </Space>
        </Header>
        <Content className="admin-content">
          <Outlet />
        </Content>
      </Layout>
      <Drawer
        placement="left"
        width={280}
        open={mobileMenuOpen}
        onClose={() => setMobileMenuOpen(false)}
        className="admin-mobile-drawer"
        title="HLW 医疗控制台"
      >
        <Menu
          mode="inline"
          items={menuItems}
          selectedKeys={navigationState.selectedKeys}
          openKeys={openKeys}
          onOpenChange={(keys) => setOpenKeys(keys as string[])}
          onClick={handleMenuClick}
        />
      </Drawer>
    </Layout>
  );
}

export default AdminLayout;
