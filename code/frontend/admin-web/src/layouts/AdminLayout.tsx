import { BellOutlined } from "@ant-design/icons";
import { Avatar, Badge, Button, Layout, Menu, Space, Tag, Typography } from "antd";
import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { appMenus } from "../router/menu";

const { Header, Content, Sider } = Layout;

export function AdminLayout() {
  const location = useLocation();
  const navigate = useNavigate();

  return (
    <Layout className="admin-shell">
      <Sider width={264} className="admin-sider">
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
          selectedKeys={[location.pathname]}
          items={appMenus.map((item) => ({
            key: item.path,
            icon: item.icon,
            label: item.label,
            onClick: () => navigate(item.path)
          }))}
        />
      </Sider>
      <Layout>
        <Header className="admin-header">
          <Space size="large">
            <div>
              <Typography.Text className="header-label">租户视角</Typography.Text>
              <div className="header-title-row">
                <Typography.Title level={4} className="header-title">
                  华林云互联网医院 SaaS
                </Typography.Title>
                <Tag color="cyan">MVP 骨架</Tag>
              </div>
            </div>
          </Space>
          <Space size="middle">
            <Badge dot>
              <Button shape="circle" icon={<BellOutlined />} />
            </Badge>
            <Space>
              <Avatar style={{ background: "#0f8fa8" }}>医</Avatar>
              <div>
                <Typography.Text strong>医院管理员</Typography.Text>
                <div className="header-label">satoken 已接入请求头</div>
              </div>
            </Space>
          </Space>
        </Header>
        <Content className="admin-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  );
}
