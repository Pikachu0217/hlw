import { Button, List, Popup, Space, SpinLoading, Tag, Toast } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  cancelAppointment,
  checkInAppointment,
  createConsultFromAppointment,
  fetchAppointments,
  fetchConsults,
  fetchPatientDoctors,
  payAppointment,
  type AppointmentItem,
  type CreatedConsult,
  type PatientDoctor
} from "../../app/api";
import { SectionCard } from "../../components/SectionCard";

/** 组合状态筛选选项（覆盖预约和问诊状态）。 */
const STATUS_OPTIONS = [
  { label: "全部", value: "" },
  { label: "待支付", value: "待支付" },
  { label: "已支付", value: "已支付" },
  { label: "已签到", value: "已签到" },
  { label: "待接单", value: "待接单" },
  { label: "咨询中", value: "咨询中" },
  { label: "已完成", value: "已完成" },
  { label: "已取消", value: "已取消" }
];

/** 统一展示条目。 */
interface FlowItem {
  /** 唯一键。 */
  key: string;
  /** 标题（预约单号/问诊单号）。 */
  title: string;
  /** 医生姓名。 */
  doctorName: string;
  /** 时间描述。 */
  timeDesc: string;
  /** 费用。 */
  feeAmount: string;
  /** 来源描述。 */
  sourceDesc: string;
  /** 展示状态。 */
  status: string;
  /** 原始状态（用于筛选匹配）。 */
  rawStatus: string;
  /** 是否可以支付。 */
  canPay: boolean;
  /** 是否可以签到。 */
  canCheckIn: boolean;
  /** 是否可以进入问诊聊天。 */
  canEnterConsult: boolean;
  /** 是否可以取消。 */
  canCancel: boolean;
  /** 关联预约单编号。 */
  appointmentId?: number;
  /** 关联问诊单编号。 */
  consultId?: number;
  /** 问诊剩余秒数。 */
  remainingSeconds?: number;
}

/**
 * 预约问诊流程页。
 * 合并展示预约单和关联问诊单的完整就医流程，支持按状态筛选和关键操作。
 */
export function ConsultFlowPage() {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState<AppointmentItem[]>([]);
  const [consults, setConsults] = useState<CreatedConsult[]>([]);
  const [doctors, setDoctors] = useState<PatientDoctor[]>([]);
  const [loading, setLoading] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string[]>([""]);
  /** 筛选弹窗是否可见。 */
  const [filterVisible, setFilterVisible] = useState(false);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData(): Promise<void> {
    setLoading(true);
    try {
      const [aptRecords, consultRecords, doctorRecords] = await Promise.all([
        fetchAppointments(),
        fetchConsults().catch(() => [] as CreatedConsult[]),
        fetchPatientDoctors()
      ]);
      setAppointments(aptRecords);
      setConsults(consultRecords);
      setDoctors(doctorRecords);
    } catch {
      Toast.show("数据加载失败");
      setAppointments([]);
      setConsults([]);
      setDoctors([]);
    } finally {
      setLoading(false);
    }
  }

  /** 找出预约单关联的问诊单。 */
  function findConsult(appointmentId: number): CreatedConsult | undefined {
    return consults.find((c) => c.appointmentId === appointmentId);
  }

  /** 构建统一展示列表。 */
  function buildFlowItems(): FlowItem[] {
    const items: FlowItem[] = [];

    // 1. 从预约单构建条目（含有关联问诊的合并展示）
    for (const apt of appointments) {
      const consult = findConsult(apt.id);
      const status = consult ? consult.status : apt.status || "待处理";
      const isAptPaid = ["已支付", "已签到", "已完成"].includes(apt.status || "");

      items.push({
        key: `apt-${apt.id}`,
        title: apt.appointmentNo || apt.patientName || "预约单",
        doctorName: apt.doctorName || "待分配医生",
        timeDesc: consult?.updatedAt ? `${apt.clinicTime || "待确认"} · ${consult.updatedAt}` : (apt.clinicTime || "待确认时间"),
        feeAmount: apt.feeAmount ?? "0.00",
        sourceDesc: apt.source || "患者端",
        status: consult ? consultStatusLabel(status) : apt.status || "待处理",
        rawStatus: consult ? consult.status : (apt.status || "待处理"),
        canPay: apt.status === "待支付",
        canCheckIn: apt.status === "已支付",
        canEnterConsult: isAptPaid && (Boolean(consult) || Boolean(apt.source) || Boolean(appointmentId)),
        canCancel: ["待支付", "已支付"].includes(apt.status || ""),
        appointmentId: apt.id,
        consultId: consult?.id,
        remainingSeconds: consult?.remainingSeconds
      });
    }

    // 2. 补充无关联预约单的独立问诊单
    const linkedAptIds = new Set(appointments.map((a) => a.id));
    for (const consult of consults) {
      if (consult.appointmentId && linkedAptIds.has(consult.appointmentId)) {
        continue; // 已通过预约单展示
      }
      items.push({
        key: `consult-${consult.id}`,
        title: consult.consultNo || `问诊单 #${consult.id}`,
        doctorName: consult.doctorName || "待分配医生",
        timeDesc: consult.updatedAt || "",
        feeAmount: consult.feeAmount || "0.00",
        sourceDesc: consult.channel || "图文问诊",
        status: consultStatusLabel(consult.status),
        rawStatus: consult.status,
        canPay: false,
        canCheckIn: false,
        canEnterConsult: ["待接单", "咨询中", "已延长"].includes(consultStatusLabel(consult.status)),
        canCancel: false,
        consultId: consult.id,
        remainingSeconds: consult.remainingSeconds
      });
    }

    // 按 ID 降序排列（最新在前）
    items.sort((a, b) => {
      const aId = parseInt(a.key.split("-")[1], 10);
      const bId = parseInt(b.key.split("-")[1], 10);
      return bId - aId;
    });

    return items;
  }

  /** 问诊展示状态文案。 */
  function consultStatusLabel(status: string): string {
    const map: Record<string, string> = {
      "待接单": "待接单",
      "咨询中": "咨询中",
      "已延长": "咨询中",
      "已完成": "已完成",
      "已取消": "已取消",
      "已超时": "已超时"
    };
    return map[status] || status || "未知";
  }

  /** 根据筛选过滤。 */
  function filterItems(items: FlowItem[]): FlowItem[] {
    const filter = statusFilter[0];
    if (!filter) return items;
    return items.filter((item) => item.rawStatus === filter);
  }

  async function handlePay(appointmentId: number): Promise<void> {
    try {
      await payAppointment(appointmentId);
      Toast.show("预约单已支付");
      await loadData();
    } catch {
      Toast.show("预约支付失败");
    }
  }

  async function handleCheckIn(appointmentId: number): Promise<void> {
    try {
      await checkInAppointment(appointmentId);
      Toast.show("签到成功");
      // 签到成功后自动创建问诊并跳转到聊天
      try {
        const consult = await createConsultFromAppointment(appointmentId);
        navigate(buildConsultChatUrl(consult));
        return;
      } catch {
        // 创建问诊失败则刷新列表，用户可手动点击"进入问诊"
        await loadData();
      }
    } catch {
      Toast.show("签到失败");
    }
  }

  async function handleCancel(appointmentId: number): Promise<void> {
    try {
      await cancelAppointment(appointmentId);
      Toast.show("预约单已取消");
      await loadData();
    } catch {
      Toast.show("取消预约失败");
    }
  }

  async function handleEnterConsult(appointmentId?: number, consultId?: number): Promise<void> {
    if (consultId) {
      const consult = consults.find((record) => record.id === consultId);
      navigate(buildConsultChatUrl({
        id: consultId,
        status: consult?.status,
        remainingSeconds: consult?.remainingSeconds
      }));
      return;
    }
    if (appointmentId) {
      try {
        const consult = await createConsultFromAppointment(appointmentId);
        navigate(buildConsultChatUrl(consult));
      } catch {
        Toast.show("进入问诊失败");
      }
    }
  }

  const flowItems = filterItems(buildFlowItems());

  return (
    <SectionCard title="预约问诊" description="查看预约和问诊记录，从挂号到问诊一站式跟进。">
      {/* 状态筛选 */}
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
        {flowItems.length === 0 && !loading ? (
          <List.Item>
            <div style={{ textAlign: "center", color: "#94a3b8", padding: "20px 0" }}>暂无记录</div>
          </List.Item>
        ) : null}

        {flowItems.map((item) => (
          <List.Item
            key={item.key}
            description={
              <Space direction="vertical" block>
                <span>{item.doctorName} · {item.timeDesc}</span>
                <span>{item.sourceDesc} · {item.feeAmount} 元</span>
                <Space wrap>
                  {item.canPay ? (
                    <Button size="mini" color="primary" onClick={() => item.appointmentId && handlePay(item.appointmentId)}>
                      支付
                    </Button>
                  ) : null}
                  {item.canCheckIn ? (
                    <Button size="mini" onClick={() => item.appointmentId && handleCheckIn(item.appointmentId)}>
                      签到
                    </Button>
                  ) : null}
                  {item.canEnterConsult ? (
                    <Button size="mini" color="primary" fill="solid" onClick={() => handleEnterConsult(item.appointmentId, item.consultId)}>
                      {item.consultId ? "进入聊天" : "进入问诊"}
                    </Button>
                  ) : null}
                  {item.canCancel ? (
                    <Button size="mini" color="default" fill="none" onClick={() => item.appointmentId && handleCancel(item.appointmentId)}>
                      取消预约
                    </Button>
                  ) : null}
                </Space>
              </Space>
            }
            extra={
              <Tag color={
                ["已完成", "已支付", "已签到"].includes(item.rawStatus) ? "success" :
                item.rawStatus === "待支付" ? "warning" :
                item.rawStatus === "咨询中" || item.rawStatus === "待接单" ? "primary" : "default"
              }>
                {item.status}
              </Tag>
            }
          >
            {item.title}
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
