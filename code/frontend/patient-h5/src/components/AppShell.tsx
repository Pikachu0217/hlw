import { NavBar, TabBar } from "antd-mobile";
import { AppOutline, CalendarOutline, MessageOutline, UnorderedListOutline, UserOutline } from "antd-mobile-icons";
import { Outlet, useLocation, useNavigate } from "react-router-dom";

const tabs = [
  { key: "/", title: "首页", icon: <AppOutline /> },
  { key: "/appointment/list", title: "挂号", icon: <CalendarOutline /> },
  { key: "/consult/chat", title: "问诊", icon: <MessageOutline /> },
  { key: "/order/list", title: "订单", icon: <UnorderedListOutline /> },
  { key: "/profile", title: "我的", icon: <UserOutline /> }
];

export function AppShell() {
  const navigate = useNavigate();
  const location = useLocation();

  return (
    <div className="mobile-shell">
      <NavBar back={null}>互联网医院</NavBar>
      <main className="mobile-main">
        <Outlet />
      </main>
      <TabBar activeKey={location.pathname} onChange={(key) => navigate(key)}>
        {tabs.map((tab) => (
          <TabBar.Item key={tab.key} icon={tab.icon} title={tab.title} />
        ))}
      </TabBar>
    </div>
  );
}
