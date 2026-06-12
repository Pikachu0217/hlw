import { Avatar, Space, Table, Tag } from "antd";
import type { ColumnsType } from "antd/es/table";

export type DoctorListItem = {
  id: number;
  name: string;
  title: string;
  consultStatus: "ONLINE" | "BUSY" | "OFFLINE";
};

type DoctorListProps = {
  doctors: DoctorListItem[];
};

const statusColorMap: Record<DoctorListItem["consultStatus"], string> = {
  ONLINE: "cyan",
  BUSY: "gold",
  OFFLINE: "default"
};

const columns: ColumnsType<DoctorListItem> = [
  {
    title: "医生",
    dataIndex: "name",
    render: (value: string) => (
      <Space>
        <Avatar style={{ background: "#0f8fa8" }}>{value.slice(0, 1)}</Avatar>
        {value}
      </Space>
    )
  },
  { title: "职称", dataIndex: "title" },
  {
    title: "接诊状态",
    dataIndex: "consultStatus",
    render: (value: DoctorListItem["consultStatus"]) => <Tag color={statusColorMap[value]}>{value}</Tag>
  }
];

export function DoctorList({ doctors }: DoctorListProps) {
  return <Table rowKey="id" columns={columns} dataSource={doctors} pagination={false} />;
}
