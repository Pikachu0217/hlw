import { Button, Space, Tag } from "antd-mobile";
import { useNavigate } from "react-router-dom";
import { createAppointment } from "../../../app/api";

type AppointmentConfirmProps = {
  doctorName: string;
  timeSlot: string;
  fee: string;
};

export function AppointmentConfirm({ doctorName, timeSlot, fee }: AppointmentConfirmProps) {
  const navigate = useNavigate();

  async function handleSubmit(): Promise<void> {
    const appointment = await createAppointment(doctorName, timeSlot);
    navigate(`/appointment/result?appointmentNo=${appointment.appointmentNo}&status=${appointment.status}`);
  }

  return (
    <div className="confirm-card">
      <Space direction="vertical" block>
        <Tag color="primary">预约确认</Tag>
        <div className="detail-title">{doctorName}</div>
        <div className="detail-copy">{timeSlot}</div>
        <div className="detail-copy">挂号费 {fee}</div>
        <Button color="primary" block onClick={handleSubmit}>
          确认提交
        </Button>
      </Space>
    </div>
  );
}
