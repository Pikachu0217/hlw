import { Button, Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";
import { PageSection } from "../../components/PageSection";

type TenantRow = {
  id: number;
  name: string;
  city: string;
  licenseNo: string;
  status: "启用" | "到期提醒";
};

const columns: ColumnsType<TenantRow> = [
  { title: "医院名称", dataIndex: "name" },
  { title: "所在城市", dataIndex: "city" },
  { title: "许可证号", dataIndex: "licenseNo" },
  {
    title: "状态",
    dataIndex: "status",
    render: (value: TenantRow["status"]) => <Tag color={value === "启用" ? "cyan" : "gold"}>{value}</Tag>
  }
];

const data: TenantRow[] = [
  { id: 1, name: "华林云总医院", city: "上海", licenseNo: "沪卫医字-001", status: "启用" },
  { id: 2, name: "江南互联网医院", city: "杭州", licenseNo: "浙卫医字-024", status: "到期提醒" }
];

export function TenantPage() {
  return (
    <PageSection
      title="租户管理"
      description="用于维护入驻医院、许可证状态与 SaaS 有效期。"
      extra={<Button type="primary">新增租户</Button>}
    >
      <Table rowKey="id" columns={columns} dataSource={data} pagination={false} />
    </PageSection>
  );
}
