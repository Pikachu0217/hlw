import { Button, List, Space, Tag } from "antd-mobile";
import { useSessionStore } from "../../store/sessionStore";
import { SectionCard } from "../../components/SectionCard";

export function ProfilePage() {
  const patientName = useSessionStore((state) => state.patientName);

  return (
    <Space direction="vertical" block style={{ "--gap": "16px" }}>
      <div className="hero-card">
        <Tag color="primary">患者档案</Tag>
        <div className="hero-title">{patientName}</div>
        <div className="hero-copy">默认已注入 mock satoken，用于后续联调接入。</div>
      </div>
      <SectionCard title="常用功能">
        <List>
          <List.Item>就诊人管理</List.Item>
          <List.Item>地址管理</List.Item>
          <List.Item>问诊记录</List.Item>
        </List>
      </SectionCard>
      <Button color="primary" block>
        联系在线客服
      </Button>
    </Space>
  );
}
