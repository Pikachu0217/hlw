import { Button, List, SpinLoading, Tag, Toast } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchConsults, type CreatedConsult } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

/**
 * 患者问诊列表页。
 * 展示当前患者的问诊单，支持进入聊天和查看状态。
 */
export function ConsultListPage() {
  const navigate = useNavigate();
  const [consults, setConsults] = useState<CreatedConsult[]>([]);
  const [loading, setLoading] = useState(false);

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

  /** 状态展示文案。 */
  function statusLabel(status: string): string {
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
    return ["待接单", "咨询中"].includes(statusLabel(status));
  }

  function isPaid(payStatus?: string): boolean {
    return payStatus === "PAID";
  }

  return (
    <SectionCard title="我的问诊" description="查看问诊记录，进入聊天或查看详情。">
      {loading ? <SpinLoading /> : null}
      <List>
        {consults.length === 0 && !loading ? (
          <List.Item>
            <div style={{ textAlign: "center", color: "#94a3b8", padding: "20px 0" }}>暂无问诊记录</div>
          </List.Item>
        ) : null}
        {consults.map((consult) => (
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
                {statusLabel(consult.status)}
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
                  onClick={() => navigate(`/consult/chat?consultId=${consult.id}&remainingSeconds=${consult.remainingSeconds ?? 0}`)}
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
