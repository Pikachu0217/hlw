import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchPatientDoctorDetail, type PatientDoctor } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";
import { AppointmentConfirm } from "./AppointmentConfirm";

export function AppointmentConfirmPage() {
  const [searchParams] = useSearchParams();
  const doctorId = Number(searchParams.get("doctorId") ?? 1);
  const [doctor, setDoctor] = useState<PatientDoctor | null>(null);

  useEffect(() => {
    let ignore = false;

    fetchPatientDoctorDetail(doctorId).then((record) => {
      if (!ignore) {
        setDoctor(record);
      }
    });

    return () => {
      ignore = true;
    };
  }, [doctorId]);

  return (
    <SectionCard title="预约挂号确认" description="确认医生、时间段和费用后即可提交订单。">
      <AppointmentConfirm doctorName={doctor?.name ?? "加载中"} timeSlot={doctor?.schedule ?? "待确认"} fee={doctor?.consultFee ?? "0.00"} />
    </SectionCard>
  );
}
