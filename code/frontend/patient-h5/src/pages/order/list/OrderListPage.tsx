import { List, Space, Tag } from "antd-mobile";
import { SectionCard } from "../../../components/SectionCard";

export function OrderListPage() {
  return (
    <SectionCard title="我的订单" description="聚合预约挂号、问诊费和药品配送订单。">
      <List>
        <List.Item
          description={
            <Space>
              <Tag color="success">已支付</Tag>
              <span>预约挂号订单</span>
            </Space>
          }
        >
          ORD20260612001
        </List.Item>
        <List.Item
          description={
            <Space>
              <Tag color="warning">待配送</Tag>
              <span>药品订单</span>
            </Space>
          }
        >
          ORD20260612002
        </List.Item>
      </List>
    </SectionCard>
  );
}
