import { Button, Space, Tag } from "antd-mobile";

type AppointmentConfirmProps = {
  doctorName: string;
  timeSlot: string;
  fee: string;
};

export function AppointmentConfirm({ doctorName, timeSlot, fee }: AppointmentConfirmProps) {
  return (
    <div className="confirm-card">
      <Space direction="vertical" block>
        <Tag color="primary">预约确认</Tag>
        <div className="detail-title">{doctorName}</div>
        <div className="detail-copy">{timeSlot}</div>
        <div className="detail-copy">挂号费 {fee}</div>
        <Button color="primary" block>
          确认提交
        </Button>
      </Space>
    </div>
  );
}
