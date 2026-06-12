import { Button, Result } from "antd-mobile";
import { useNavigate, useSearchParams } from "react-router-dom";

export function AppointmentResultPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const appointmentNo = searchParams.get("appointmentNo") ?? "";
  const status = searchParams.get("status") ?? "";

  return (
    <div className="mobile-stack">
      <Result status="success" title="预约提交成功" description={`${appointmentNo} ${status}`} />
      <Button color="primary" block onClick={() => navigate("/order/list")}>
        查看订单
      </Button>
    </div>
  );
}
