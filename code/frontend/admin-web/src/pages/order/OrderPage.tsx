import { Segmented, Space, Table, Tag } from "antd";
import { useState } from "react";
import { PageSection } from "../../components/PageSection";

const data = [
  { id: 1, orderNo: "ORD20260612001", amount: "68.00", status: "已支付", type: "预约挂号" },
  { id: 2, orderNo: "ORD20260612002", amount: "129.00", status: "待配送", type: "药品订单" }
];

export function OrderPage() {
  const [segment, setSegment] = useState("全部订单");

  return (
    <PageSection title="订单中心" description="统一查看预约、问诊和药品相关订单状态。">
      <Space direction="vertical" size={20} style={{ width: "100%" }}>
        <Segmented
          options={["全部订单", "待支付", "待配送", "已完成"]}
          value={segment}
          onChange={(value) => setSegment(String(value))}
        />
        <Table
          rowKey="id"
          pagination={false}
          dataSource={data}
          columns={[
            { title: "订单号", dataIndex: "orderNo" },
            { title: "业务类型", dataIndex: "type" },
            { title: "订单金额", dataIndex: "amount" },
            {
              title: "状态",
              dataIndex: "status",
              render: (value: string) => <Tag color={value === "已支付" ? "cyan" : "gold"}>{value}</Tag>
            }
          ]}
        />
      </Space>
    </PageSection>
  );
}
