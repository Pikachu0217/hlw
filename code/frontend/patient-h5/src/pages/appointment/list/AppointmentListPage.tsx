import { Button, List, Space, SpinLoading, Tag, Toast } from "antd-mobile";
import { useEffect, useState } from "react";
import {
  checkInAppointment,
  fetchAppointments,
  fetchPatientDoctors,
  grabAppointment,
  payAppointment,
  type AppointmentItem,
  type PatientDoctor
} from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

export function AppointmentListPage() {
  const [appointments, setAppointments] = useState<AppointmentItem[]>([]);
  const [doctors, setDoctors] = useState<PatientDoctor[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    void loadAppointments();
  }, []);

  async function loadAppointments(): Promise<void> {
    setLoading(true);
    try {
      const [appointmentRecords, doctorRecords] = await Promise.all([fetchAppointments(), fetchPatientDoctors()]);
      setAppointments(appointmentRecords);
      setDoctors(doctorRecords);
    } catch {
      Toast.show("预约列表加载失败");
      setAppointments([]);
      setDoctors([]);
    } finally {
      setLoading(false);
    }
  }

  async function handlePay(appointmentId: number): Promise<void> {
    try {
      await payAppointment(appointmentId);
      Toast.show("预约单已支付");
      await loadAppointments();
    } catch {
      Toast.show("预约支付失败");
    }
  }

  async function handleCheckIn(appointmentId: number): Promise<void> {
    try {
      await checkInAppointment(appointmentId);
      Toast.show("签到成功");
      await loadAppointments();
    } catch {
      Toast.show("签到失败");
    }
  }

  async function handleGrab(appointmentId: number): Promise<void> {
    const doctorId = doctors.find((doctor) => doctor.doctorId)?.doctorId;
    if (!doctorId) {
      Toast.show("暂无可用医生");
      return;
    }

    try {
      await grabAppointment(appointmentId, doctorId);
      Toast.show("便民门诊抢单成功");
      await loadAppointments();
    } catch {
      Toast.show("便民门诊抢单失败");
    }
  }

  function resolveStatus(appointment: AppointmentItem): string {
    return appointment.status || "待处理";
  }

  const appointmentList = Array.isArray(appointments) ? appointments : [];

  return (
    <SectionCard title="我的预约" description="查看预约单，并执行支付、签到和便民门诊抢单。">
      {loading ? <SpinLoading /> : null}
      <List>
        {appointmentList.map((appointment) => (
          <List.Item
            key={appointment.id}
            description={
              <Space direction="vertical" block>
                <span>{appointment.doctorName || "待分配医生"} · {appointment.clinicTime || "待确认时间"}</span>
                <span>{appointment.source || "患者端"} · {appointment.feeAmount ?? "0.00"} 元</span>
                <Space wrap>
                  <Button size="mini" color="primary" onClick={() => handlePay(appointment.id)}>
                    支付
                  </Button>
                  <Button size="mini" onClick={() => handleCheckIn(appointment.id)}>
                    签到
                  </Button>
                  <Button size="mini" onClick={() => handleGrab(appointment.id)}>
                    抢单
                  </Button>
                </Space>
              </Space>
            }
            extra={<Tag color={resolveStatus(appointment).includes("已") ? "success" : "warning"}>{resolveStatus(appointment)}</Tag>}
          >
            {appointment.appointmentNo || appointment.patientName || "预约单"}
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
