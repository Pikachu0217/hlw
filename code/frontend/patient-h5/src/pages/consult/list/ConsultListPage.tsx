import { Button, List, Popup, SpinLoading, Tag, Toast } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchConsults, type CreatedConsult } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

/** 问诊状态筛选选项（与后端 ConsultStatus label 一致）。 */
const STATUS_OPTIONS = [
  { label: "全部", value: "" },
  { label: "待接单", value: "待接单" },
  { label: "咨询中", value: "咨询中" },
  { label: "已完成", value: "已完成" },
  { label: "已取消", value: "已取消" },
  { label: "已超时", value: "已超时" }
];

/**
 * 患者问诊列表页。
 * 展示当前患者的问诊单，支持按状态筛选，进入聊天或查看详情。
 */
export function ConsultListPage() {
  const navigate = useNavigate();
  const [consults, setConsults] = useState<CreatedConsult[]>([]);
  const [loading, setLoading] = useState(false);
  /** 当前选中的状态筛选值，空字符串表示全部。 */
  const [statusFilter, setStatusFilter] = useState<string[]>([""]);
  /** 筛选弹窗是否可见。 */
  const [filterVisible, setFilterVisible] = useState(false);

  useEffect(() => {
    void loadConsults();
  }, []);

  async function loadConsults(): Promise<void> {
    setLoading(true);
    try {
      const records = await fetchConsults();
      setConsults(Array.isArray(records) ? records : []);
    } catch {
      Toast.show("问诊列表加载失败");
      setConsults([]);
    } finally {
      setLoading(false);
    }
  }

  /** 根据当前筛选条件过滤问诊单。 */
  function filteredConsults(): CreatedConsult[] {
    const filterValue = statusFilter[0];
    if (!filterValue) {
      return consults;
    }
    return consults.filter((c) => c.status === filterValue);
  }

  /** 状态展示文案。 */
  function displayStatus(status: string): string {
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

  function isActive(status: string): boolean {
    return ["待接单", "咨询中"].includes(displayStatus(status));
  }

  function isPaid(payStatus?: string): boolean {
    return payStatus === "PAID";
  }

  const displayList = filteredConsults();

  return (
    <SectionCard title="我的问诊" description="查看问诊记录，进入聊天或查看详情。">
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
            <div style={{ textAlign: "center", color: "#94a3b8", padding: "20px 0" }}>暂无问诊记录</div>
          </List.Item>
        ) : null}

        {displayList.map((consult) => (
          <List.Item
            key={consult.id}
            description={
              <div>
                <div>{consult.doctorName || "待分配医生"} · {consult.channel || "图文问诊"}</div>
                <div style={{ marginTop: 4 }}>
                  {consult.feeAmount ? `${consult.feeAmount} 元` : ""}
                  {consult.updatedAt ? ` · ${consult.updatedAt}` : ""}
                </div>
              </div>
            }
            extra={
              <Tag color={isActive(consult.status) ? "success" : "default"}>
                {displayStatus(consult.status)}
              </Tag>
            }
          >
            {consult.consultNo || `问诊单 #${consult.id}`}
            <div style={{ marginTop: 4 }}>
              {isPaid(consult.payStatus) && isActive(consult.status) ? (
                <Button
                  size="mini"
                  color="primary"
                  fill="solid"
                  onClick={() => navigate(buildConsultChatUrl(consult))}
                >
                  进入聊天
                </Button>
              ) : null}
              {!isPaid(consult.payStatus) ? (
                <Tag color="warning" style={{ marginLeft: 8 }}>未支付</Tag>
              ) : null}
            </div>
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
