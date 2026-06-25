import { Button, List, Popup, Space, SpinLoading, Tag, Toast } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  checkInAppointment,
  createConsultFromAppointment,
  fetchAppointments,
  fetchPatientDoctors,
  grabAppointment,
  payAppointment,
  type AppointmentItem,
  type PatientDoctor
} from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

/** 预约状态选项列表（与后端 AppointmentStatus 中文值一致）。 */
const STATUS_OPTIONS = [
  { label: "全部", value: "" },
  { label: "待支付", value: "待支付" },
  { label: "已支付", value: "已支付" },
  { label: "已签到", value: "已签到" },
  { label: "已完成", value: "已完成" },
  { label: "已取消", value: "已取消" },
  { label: "已拒诊", value: "已拒诊" },
  { label: "已接单", value: "已接单" }
];

/**
 * 患者预约单列表页。
 * 展示当前患者的预约单，支持按状态筛选，以及支付、签到、进入问诊等操作。
 */
export function AppointmentListPage() {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState<AppointmentItem[]>([]);
  const [doctors, setDoctors] = useState<PatientDoctor[]>([]);
  const [loading, setLoading] = useState(false);
  /** 当前选中的状态筛选值，空字符串表示全部。 */
  const [statusFilter, setStatusFilter] = useState<string[]>([""]);
  /** 筛选弹窗是否可见。 */
  const [filterVisible, setFilterVisible] = useState(false);

  useEffect(() => {
    void loadAppointments();
  }, []);

  async function loadAppointments(): Promise<void> {
    setLoading(true);
    try {
      const [appointmentRecords, doctorRecords] = await Promise.all([
        fetchAppointments(),
        fetchPatientDoctors()
      ]);
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

  /** 根据当前筛选条件过滤预约单。 */
  function filteredAppointments(): AppointmentItem[] {
    const filterValue = statusFilter[0];
    if (!filterValue) {
      return appointments;
    }
    return appointments.filter((a) => a.status === filterValue);
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

  /** 进入问诊聊天（自动创建关联问诊单，幂等）。 */
  async function handleEnterConsult(appointmentId: number): Promise<void> {
    try {
      const consult = await createConsultFromAppointment(appointmentId);
      navigate(buildConsultChatUrl(consult));
    } catch {
      Toast.show("进入问诊失败");
    }
  }

  /** 是否已支付（含已签到、已完成）。后端返回中文状态值。 */
  function isPaid(status: string): boolean {
    return ["已支付", "已签到", "已完成"].includes(status);
  }

  /** 是否已结束且不可继续操作。 */
  function isClosed(status: string): boolean {
    return ["已取消", "已拒诊"].includes(status);
  }

  /** 是否已签到（仅当已支付且未完成时可签到）。 */
  function canCheckIn(status: string): boolean {
    return status === "已支付";
  }

  /** 是否可以进入问诊（已支付后即可，不限于已签到）。 */
  function canConsult(status: string): boolean {
    return isPaid(status);
  }

  const displayList = filteredAppointments();

  return (
    <SectionCard title="我的预约" description="查看预约单，并执行支付、签到和图文问诊。">
      {/* 状态筛选行 */}
      <div style={{ padding: "8px 16px 0" }}>
        <span
          style={{ fontSize: 14, color: "#1677ff", cursor: "pointer" }}
          onClick={() => setFilterVisible(true)}
        >
          {STATUS_OPTIONS.find((o) => o.value === statusFilter[0])?.label || "全部"} ▾
        </span>
      </div>

      {/* 状态筛选弹窗 */}
      <Popup
        visible={filterVisible}
        onMaskClick={() => setFilterVisible(false)}
        bodyStyle={{ borderTopLeftRadius: 16, borderTopRightRadius: 16, padding: "16px 0" }}
      >
        <div style={{ padding: "0 16px 12px", fontSize: 16, fontWeight: 600, color: "#1f3a33" }}>筛选状态</div>
        {STATUS_OPTIONS.map((opt) => {
          const active = opt.value === statusFilter[0];
          return (
            <div
              key={opt.value}
              style={{
                padding: "10px 16px",
                fontSize: 15,
                color: active ? "#1677ff" : "#333",
                background: active ? "rgba(22,119,255,0.06)" : "transparent",
                cursor: "pointer"
              }}
              onClick={() => {
                setStatusFilter([opt.value]);
                setFilterVisible(false);
              }}
            >
              {opt.label}
              {active ? <span style={{ float: "right", color: "#1677ff" }}>✓</span> : null}
            </div>
          );
        })}
        <div style={{ padding: "12px 16px 0" }}>
          <Button block size="small" onClick={() => setFilterVisible(false)}>取消</Button>
        </div>
      </Popup>

      {loading ? <SpinLoading style={{ display: "block", margin: "20px auto" }} /> : null}

      <List>
        {displayList.length === 0 && !loading ? (
          <List.Item>
            <div style={{ textAlign: "center", color: "#94a3b8", padding: "20px 0" }}>暂无预约记录</div>
          </List.Item>
        ) : null}

        {displayList.map((appointment) => (
          <List.Item
            key={appointment.id}
            description={
              <Space direction="vertical" block>
                <span>{appointment.doctorName || "待分配医生"} · {appointment.clinicTime || "待确认时间"}</span>
                <span>{appointment.source || "患者端"} · {appointment.feeAmount ?? "0.00"} 元</span>
                <Space wrap>
                  {appointment.status === "待支付" ? (
                    <Button size="mini" color="primary" onClick={() => handlePay(appointment.id)}>
                      支付
                    </Button>
                  ) : null}
                  {canCheckIn(appointment.status) ? (
                    <Button size="mini" onClick={() => handleCheckIn(appointment.id)}>
                      签到
                    </Button>
                  ) : null}
                  {canConsult(appointment.status) ? (
                    <Button size="mini" color="primary" fill="solid" onClick={() => handleEnterConsult(appointment.id)}>
                      进入问诊
                    </Button>
                  ) : null}
                  {appointment.status === "待支付" ? (
                    <Button size="mini" onClick={() => handleGrab(appointment.id)}>
                      抢单
                    </Button>
                  ) : null}
                </Space>
              </Space>
            }
            extra={<Tag color={isClosed(appointment.status) ? "danger" : isPaid(appointment.status) ? "success" : "warning"}>{appointment.status || "待处理"}</Tag>}
          >
            {appointment.appointmentNo || appointment.patientName || "预约单"}
          </List.Item>
        ))}
      </List>
    </SectionCard>
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
