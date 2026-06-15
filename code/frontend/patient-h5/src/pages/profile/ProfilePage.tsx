import { Button, List, Space, Tag } from "antd-mobile";
import { useEffect } from "react";
import { fetchPatientProfile } from "../../app/api";
import { useSessionStore } from "../../store/sessionStore";
import { SectionCard } from "../../components/SectionCard";

export function ProfilePage() {
  const patientName = useSessionStore((state) => state.patientName);
  const setPatientName = useSessionStore((state) => state.setPatientName);

  useEffect(() => {
    fetchPatientProfile()
      .then((profile) => setPatientName(profile.name))
      .catch(() => {
        console.warn("[patient] 患者服务未连接，使用本地患者名称");
      });
  }, [setPatientName]);

  return (
    <Space direction="vertical" block className="mobile-stack">
      <div className="hero-card">
        <Tag color="primary">患者档案</Tag>
        <div className="hero-title">{patientName}</div>
        <div className="hero-copy">默认已注入本地登录令牌，用于访问患者服务接口。</div>
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
