import { Button, Result } from "antd-mobile";
import { useNavigate } from "react-router-dom";

export function AppointmentResultPage() {
  const navigate = useNavigate();

  return (
    <Result
      status="success"
      title="预约提交成功"
      description="已为你锁定上午号源，请在规定时间内完成支付。"
      extra={<Button onClick={() => navigate("/order/list")}>查看订单</Button>}
    />
  );
}
