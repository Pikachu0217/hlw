import { Descriptions, List, Space, Tag } from "antd";
import { PageSection } from "../../components/PageSection";

export function PatientPage() {
  return (
    <Space direction="vertical" size={24} className="stack-full">
      <PageSection title="患者档案" description="展示患者基础资料、最近问诊与健康档案摘要。">
        <Descriptions bordered column={2}>
          <Descriptions.Item label="患者姓名">王小雨</Descriptions.Item>
          <Descriptions.Item label="手机号">138****8821</Descriptions.Item>
          <Descriptions.Item label="最近问诊">儿科图文问诊</Descriptions.Item>
          <Descriptions.Item label="风险标签">
            <Tag color="gold">过敏史</Tag>
          </Descriptions.Item>
        </Descriptions>
      </PageSection>
      <PageSection title="健康记录" description="后续可接入体温、既往病史和随访信息。">
        <List
          dataSource={["青霉素过敏", "2026-06-10 血压偏高", "2026-06-11 儿科复诊记录已归档"]}
          renderItem={(item) => <List.Item>{item}</List.Item>}
        />
      </PageSection>
    </Space>
  );
}
