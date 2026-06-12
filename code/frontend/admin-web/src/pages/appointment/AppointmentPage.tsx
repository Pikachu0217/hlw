import { Button, Space, Table, Tag } from "antd";
import { PageSection } from "../../components/PageSection";

const data = [
  { id: 1, doctor: "李医生", slot: "上午", remain: 6, status: "放号中" },
  { id: 2, doctor: "周医生", slot: "下午", remain: 1, status: "紧张" }
];

export function AppointmentPage() {
  return (
    <PageSection
      title="预约挂号"
      description="围绕号源、预约单和便民门诊抢单状态提供统一管理入口。"
      extra={<Button type="primary">新增放号配置</Button>}
    >
      <Table
        rowKey="id"
        pagination={false}
        dataSource={data}
        columns={[
          { title: "医生", dataIndex: "doctor" },
          { title: "时段", dataIndex: "slot" },
          { title: "剩余号源", dataIndex: "remain" },
          {
            title: "状态",
            dataIndex: "status",
            render: (value: string) => <Tag color={value === "放号中" ? "cyan" : "gold"}>{value}</Tag>
          },
          {
            title: "操作",
            render: () => (
              <Space>
                <Button type="link">锁号记录</Button>
                <Button type="link">查看预约</Button>
              </Space>
            )
          }
        ]}
      />
    </PageSection>
  );
}
