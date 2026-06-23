import { Button, Form, Input, List, Picker, Space, SpinLoading, Tag, Toast } from "antd-mobile";
import { useEffect, useState } from "react";
import { createOrder, fetchOrders, fetchPatientProfile, payOrder, type OrderItem } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

interface OrderFormValues {
  businessType?: string[];
  amount?: string;
}

export function OrderListPage() {
  const [orders, setOrders] = useState<OrderItem[]>([]);
  const [loading, setLoading] = useState(false);
  const [form] = Form.useForm<OrderFormValues>();

  useEffect(() => {
    void loadOrders();
  }, []);

  async function loadOrders(): Promise<void> {
    setLoading(true);

    try {
      setOrders(await fetchOrders());
    } finally {
      setLoading(false);
    }
  }

  async function handleCreateOrder(): Promise<void> {
    const values = await form.validateFields();
    try {
      const profile = await fetchPatientProfile();
      await createOrder({
        bizType: values.businessType?.[0] ?? "CONSULT",
        patientId: profile.id,
        patientName: profile.patientName,
        amount: values.amount ?? "0.00",
        createdAt: new Date().toLocaleString()
      });
      form.resetFields();
      Toast.show("订单已创建");
      await loadOrders();
    } catch {
      Toast.show("订单创建失败");
    }
  }

  async function handlePay(orderId: number, payMethod: string): Promise<void> {
    try {
      await payOrder(orderId, payMethod);
      Toast.show("订单已支付");
      await loadOrders();
    } catch {
      Toast.show("订单支付失败");
    }
  }

  return (
    <SectionCard title="我的订单" description="聚合预约挂号、问诊费和药品配送订单。">
      <Form
        form={form}
        layout="horizontal"
        className="order-create-form"
        footer={<Button color="primary" block onClick={handleCreateOrder}>创建订单</Button>}
      >
        <Form.Item label="业务类型" name="businessType" rules={[{ required: true, message: "请选择业务类型" }]}>
          <Picker columns={[[{ label: "问诊订单", value: "CONSULT" }, { label: "挂号订单", value: "APPOINTMENT" }, { label: "药品订单", value: "DRUG" }]]}>
            {(items) => <Input readOnly value={typeof items?.[0]?.label === "string" ? items[0].label : ""} placeholder="请选择业务类型" />}
          </Picker>
        </Form.Item>
        <Form.Item label="金额" name="amount" rules={[{ required: true, message: "请输入金额" }]}>
          <Input type="number" placeholder="请输入订单金额" />
        </Form.Item>
      </Form>
      {loading ? <SpinLoading /> : null}
      <List>
        {orders.map((order) => (
          <List.Item
            key={order.id}
            description={
              <Space>
                <Tag color={order.payStatus.includes("已") ? "success" : "warning"}>{order.payStatus}</Tag>
                <span>{order.businessType}</span>
                <Button size="mini" onClick={() => handlePay(order.id, "WECHAT")}>
                  微信支付
                </Button>
                <Button size="mini" onClick={() => handlePay(order.id, "ALI_PAY")}>
                  支付宝
                </Button>
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
