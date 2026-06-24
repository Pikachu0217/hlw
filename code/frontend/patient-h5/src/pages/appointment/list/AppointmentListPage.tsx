import { Button, List, Space, SpinLoading, Tag, Toast } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  checkInAppointment,
  createConsultFromAppointment,
  fetchAppointments,
  fetchConsults,
  fetchPatientDoctors,
  grabAppointment,
  payAppointment,
  type AppointmentItem,
  type CreatedConsult,
  type PatientDoctor
} from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

export function AppointmentListPage() {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState<AppointmentItem[]>([]);
  const [consults, setConsults] = useState<CreatedConsult[]>([]);
  const [doctors, setDoctors] = useState<PatientDoctor[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    void loadAppointments();
  }, []);

  async function loadAppointments(): Promise<void> {
    setLoading(true);
    try {
      const [appointmentRecords, consultRecords, doctorRecords] = await Promise.all([
        fetchAppointments(),
        fetchConsults().catch(() => [] as CreatedConsult[]),
        fetchPatientDoctors()
      ]);
      setAppointments(appointmentRecords);
      setConsults(consultRecords);
      setDoctors(doctorRecords);
    } catch {
      Toast.show("预约列表加载失败");
      setAppointments([]);
      setConsults([]);
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

  /** 查找预约单关联的问诊单 */
  function findConsult(appointmentId: number): CreatedConsult | undefined {
    return consults.find((c) => c.appointmentId === appointmentId);
  }

  /** 进入问诊聊天（关联问诊单不存在时先创建） */
  async function handleEnterConsult(appointmentId: number): Promise<void> {
    try {
      const consult = await createConsultFromAppointment(appointmentId);
      navigate(`/consult/chat?consultId=${consult.id}`);
    } catch {
      Toast.show("进入问诊失败");
    }
  }

  function resolveStatus(appointment: AppointmentItem): string {
    return appointment.status || "待处理";
  }

  /** 是否已支付 */
  function isPaid(appointment: AppointmentItem): boolean {
    return ["PAID", "CHECKED_IN", "COMPLETED"].includes(appointment.status);
  }

  const appointmentList = Array.isArray(appointments) ? appointments : [];

  return (
    <SectionCard title="我的预约" description="查看预约单，并执行支付、签到和图文问诊。">
      {loading ? <SpinLoading /> : null}
      <List>
        {appointmentList.map((appointment) => {
          const consult = findConsult(appointment.id);
          return (
            <List.Item
              key={appointment.id}
              description={
                <Space direction="vertical" block>
                  <span>{appointment.doctorName || "待分配医生"} · {appointment.clinicTime || "待确认时间"}</span>
                  <span>{appointment.source || "患者端"} · {appointment.feeAmount ?? "0.00"} 元</span>
                  <Space wrap>
                    {!isPaid(appointment) ? (
                      <Button size="mini" color="primary" onClick={() => handlePay(appointment.id)}>
                        支付
                      </Button>
                    ) : null}
                    {isPaid(appointment) && appointment.source !== "CONSULT" ? (
                      <Button size="mini" onClick={() => handleCheckIn(appointment.id)}>
                        签到
                      </Button>
                    ) : null}
                    {isPaid(appointment) && (consult || appointment.source === "CONSULT") ? (
                      <Button size="mini" color="primary" fill="solid" onClick={() => handleEnterConsult(appointment.id)}>
                        进入问诊
                      </Button>
                    ) : null}
                    <Button size="mini" onClick={() => handleGrab(appointment.id)}>
                      抢单
                    </Button>
                  </Space>
                </Space>
              }
              extra={<Tag color={isPaid(appointment) ? "success" : "warning"}>{resolveStatus(appointment)}</Tag>}
            >
              {appointment.appointmentNo || appointment.patientName || "预约单"}
            </List.Item>
          );
        })}
      </List>
    </SectionCard>
  );
}
