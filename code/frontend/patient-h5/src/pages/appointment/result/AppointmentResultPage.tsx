import { Button, Result, Space, Toast } from "antd-mobile";
import { useNavigate, useSearchParams } from "react-router-dom";
import { checkInAppointment, createConsultFromAppointment, payAppointment } from "../../../app/api";

export function AppointmentResultPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const appointmentNo = searchParams.get("appointmentNo") ?? "";
  const status = searchParams.get("status") ?? "";
  const appointmentId = Number(searchParams.get("appointmentId") ?? 0);
  const source = searchParams.get("source") ?? "";

  async function handlePay(): Promise<void> {
    if (!appointmentId) {
      Toast.show("缺少预约编号");
      return;
    }

    try {
      await payAppointment(appointmentId);
      Toast.show("预约单已支付");

      // 如果是图文问诊入口，支付后自动创建问诊单并跳转到聊天
      if (source === "consult") {
        const consult = await createConsultFromAppointment(appointmentId);
        navigate(buildConsultChatUrl(consult), { replace: true });
        return;
      }

      navigate(`/appointment/result?appointmentNo=${appointmentNo}&status=PAID&appointmentId=${appointmentId}`, { replace: true });
    } catch {
      Toast.show("预约支付失败");
    }
  }

  async function handleCheckIn(): Promise<void> {
    if (!appointmentId) {
      Toast.show("缺少预约编号");
      return;
    }

    try {
      await checkInAppointment(appointmentId);
      Toast.show("签到成功");
    } catch {
      Toast.show("签到失败");
    }
  }

  return (
    <div className="mobile-stack">
      <Result status="success" title="预约提交成功" description={`${appointmentNo} ${status}`} />
      <Space direction="vertical" block>
        <Button color="primary" block onClick={handlePay}>
          支付预约单
        </Button>
        <Button block onClick={handleCheckIn}>
          到院签到
        </Button>
        <Button block onClick={() => navigate("/appointment/list")}>
          查看预约
        </Button>
      </Space>
    </div>
  );
}

function buildConsultChatUrl(consult: { id: number; status?: string; remainingSeconds?: number }): string {
  const params = new URLSearchParams({
    consultId: String(consult.id),
    status: consult.status ?? "",
    remainingSeconds: String(consult.remainingSeconds ?? 0)
  });
  return `/consult/chat?${params.toString()}`;
}
