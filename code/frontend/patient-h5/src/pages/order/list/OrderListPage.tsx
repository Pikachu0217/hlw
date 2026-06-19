import { List, Space, SpinLoading, Tag } from "antd-mobile";
import { useEffect, useState } from "react";
import { fetchOrders, type OrderItem } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

export function OrderListPage() {
  const [orders, setOrders] = useState<OrderItem[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    fetchOrders()
      .then((records) => {
        if (!ignore) {
          setOrders(records);
        }
      })
      .finally(() => {
        if (!ignore) {
          setLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  return (
    <SectionCard title="我的订单" description="聚合预约挂号、问诊费和药品配送订单。">
      {loading ? <SpinLoading /> : null}
      <List>
        {orders.map((order) => (
          <List.Item
            key={order.id}
            description={
              <Space>
                <Tag color={order.payStatus.includes("已") ? "success" : "warning"}>{order.payStatus}</Tag>
                <span>{order.businessType}</span>
              </Space>
            }
          >
            {order.orderNo}
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
