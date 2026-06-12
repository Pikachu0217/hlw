import { Button, Descriptions, List, Space, Tag } from "antd";
import { PageSection } from "../../components/PageSection";

export function PrescriptionPage() {
  return (
    <Space direction="vertical" size={24} style={{ width: "100%" }}>
      <PageSection
        title="处方审核"
        description="支持待审、已通过、已驳回三类处方的审核流转查看。"
        extra={<Button type="primary">导出审核记录</Button>}
      >
        <Descriptions bordered column={2}>
          <Descriptions.Item label="待审处方">12</Descriptions.Item>
          <Descriptions.Item label="今日通过率">93%</Descriptions.Item>
          <Descriptions.Item label="平均审核时长">6 分钟</Descriptions.Item>
          <Descriptions.Item label="事件状态">
            <Tag color="cyan">prescription.audited</Tag>
          </Descriptions.Item>
        </Descriptions>
      </PageSection>
      <PageSection title="待审清单" description="后续可接入真实处方项、患者信息和药师审核意见。">
        <List
          dataSource={["处方 #10021 - 儿科感冒用药", "处方 #10022 - 内科复诊续方"]}
          renderItem={(item) => <List.Item actions={[<Button type="link" key="approve">审核</Button>]}>{item}</List.Item>}
        />
      </PageSection>
    </Space>
  );
}
