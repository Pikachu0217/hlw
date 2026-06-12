import { Button, Space, Table, Tag } from "antd";
import { PageSection } from "../../../components/PageSection";

const data = [
  { id: 1, username: "admin_sh", role: "医院管理员", status: "启用" },
  { id: 2, username: "pharmacist_01", role: "药师", status: "启用" },
  { id: 3, username: "doctor_li", role: "医生", status: "停用" }
];

export function UserPage() {
  return (
    <PageSection
      title="用户管理"
      description="统一维护医院管理员、医生、药师等账号与角色映射。"
      extra={<Button type="primary">新增用户</Button>}
    >
      <Table
        rowKey="id"
        dataSource={data}
        pagination={false}
        columns={[
          { title: "账号", dataIndex: "username" },
          { title: "角色", dataIndex: "role" },
          {
            title: "状态",
            dataIndex: "status",
            render: (value: string) => <Tag color={value === "启用" ? "cyan" : "default"}>{value}</Tag>
          },
          {
            title: "操作",
            render: () => (
              <Space>
                <Button type="link">编辑</Button>
                <Button type="link">重置密码</Button>
              </Space>
            )
          }
        ]}
      />
    </PageSection>
  );
}
