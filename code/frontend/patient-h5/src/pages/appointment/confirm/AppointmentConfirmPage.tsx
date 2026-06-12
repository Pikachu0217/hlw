import { SectionCard } from "../../../components/SectionCard";
import { AppointmentConfirm } from "./AppointmentConfirm";

export function AppointmentConfirmPage() {
  return (
    <SectionCard title="预约挂号确认" description="确认医生、时间段和费用后即可提交订单。">
      <AppointmentConfirm doctorName="李医生" timeSlot="上午" fee="50.00" />
    </SectionCard>
  );
}
