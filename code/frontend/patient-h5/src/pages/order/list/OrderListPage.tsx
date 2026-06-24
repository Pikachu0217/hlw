import { List, Space, SpinLoading, Tag, Toast } from "antd-mobile";
import { useEffect, useState } from "react";
import { fetchOrders, type OrderItem } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

/**
 * 患者订单列表页。
 * 订单是平台支付交易流水记录，涵盖挂号费、问诊费和药品配送费的支付凭证。
 * 当您支付预约挂号、图文问诊或购买药品时，系统会自动生成对应的订单记录。
 */
export function OrderListPage() {
  const [orders, setOrders] = useState<OrderItem[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    void loadOrders();
  }, []);

  async function loadOrders(): Promise<void> {
    setLoading(true);

    try {
      setOrders(await fetchOrders());
    } catch {
      Toast.show("订单列表加载失败");
      setOrders([]);
    } finally {
      setLoading(false);
    }
  }

  /** 订单类型中文映射。 */
  function businessTypeLabel(bizType?: string): string {
    const map: Record<string, string> = {
      "门诊预约": "挂号费",
      "图文咨询": "问诊费",
      "处方购药": "药品费",
      "药品配送": "配送费"
    };
    return map[bizType || ""] || bizType || "其他";
  }

  return (
    <SectionCard title="我的订单" description="挂号费、问诊费和药品配送费的支付记录。">
      {loading ? <SpinLoading style={{ display: "block", margin: "20px auto" }} /> : null}

      <List>
        {orders.length === 0 && !loading ? (
          <List.Item>
            <div style={{ textAlign: "center", color: "#94a3b8", padding: "20px 0" }}>暂无订单记录</div>
          </List.Item>
        ) : null}

        {orders.map((order) => (
          <List.Item
            key={order.id}
            description={
              <Space>
                <span>{order.amount}</span>
                <Tag color={order.payStatus === "已支付" ? "success" : "warning"}>{order.payStatus}</Tag>
              </Space>
            }
          >
            <Space>
              <span>{order.orderNo}</span>
              <Tag color="primary" fill="outline">{businessTypeLabel(order.businessType)}</Tag>
            </Space>
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
