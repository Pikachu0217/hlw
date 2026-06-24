import { useEffect, useRef, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Toast } from "antd-mobile";
import { completeConsult, extendConsult, fetchConsultMessages, type ConsultMessageItem } from "../../../app/api";
import { AUTHORIZATION_TOKEN_PREFIX } from "../../../app/auth-header";
import { useSessionStore } from "../../../store/sessionStore";
import { ConsultChat } from "./ConsultChat";

export function ConsultChatPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const consultId = Number(searchParams.get("consultId") ?? 0);
  const [messages, setMessages] = useState<ConsultMessageItem[]>([]);
  const [textMessage, setTextMessage] = useState("");
  const [doctorName, setDoctorName] = useState("医生");
  const remainingSeconds = Number(searchParams.get("remainingSeconds") ?? 0);
  const socketRef = useRef<WebSocket | null>(null);

  // 加载历史消息
  useEffect(() => {
    let ignore = false;

    if (!consultId) {
      setMessages([]);
      return () => {
        ignore = true;
      };
    }

    fetchConsultMessages(consultId)
      .then((records) => {
        if (!ignore) {
          setMessages(records);
          // 从消息记录中提取医生姓名
          const doctorMsg = records.find((m) => m.senderType === "DOCTOR");
          if (doctorMsg) {
            setDoctorName("医生");
          }
        }
      });

    return () => {
      ignore = true;
    };
  }, [consultId]);

  // WebSocket 连接
  useEffect(() => {
    let cancelled = false;
    let connectTimer: number | undefined;

    socketRef.current?.close();
    socketRef.current = null;

    if (!consultId) {
      return undefined;
    }

    connectTimer = window.setTimeout(() => {
      if (cancelled) {
        return;
      }

      const socket = new WebSocket(resolveConsultWsUrl(consultId));
      socketRef.current = socket;
      socket.onmessage = (event) => {
        try {
          const payload = JSON.parse(event.data) as ConsultMessageItem;
          setMessages((items) => [...items, payload]);
        } catch {
          console.warn("[consult-ws] 消息解析失败", event.data);
        }
      };
      socket.onopen = () => console.info("[consult-ws] 问诊连接已建立", consultId);
      socket.onclose = (event) => console.warn("[consult-ws] 问诊连接已关闭", consultId, event.code, event.reason);
      socket.onerror = () => {
        if (!cancelled) {
          Toast.show("问诊连接异常，请稍后重试");
        }
      };
    }, 0);

    return () => {
      cancelled = true;
      if (connectTimer !== undefined) {
        window.clearTimeout(connectTimer);
      }
      socketRef.current?.close();
      socketRef.current = null;
    };
  }, [consultId]);

  /** 发送消息。 */
  function sendMessage(contentType: "TEXT" | "IMAGE", content: string): void {
    const trimmedContent = content.trim();
    if (!trimmedContent) {
      Toast.show(contentType === "TEXT" ? "请输入消息内容" : "请上传图片");
      return;
    }
    if (!socketRef.current || socketRef.current.readyState !== WebSocket.OPEN) {
      Toast.show("问诊连接中，请稍后重试");
      return;
    }
    socketRef.current.send(JSON.stringify({ contentType, content: trimmedContent }));
    if (contentType === "TEXT") {
      setTextMessage("");
    }
  }

  async function handleExtend(): Promise<void> {
    if (!consultId) {
      Toast.show("缺少问诊编号");
      return;
    }
    try {
      await extendConsult(consultId);
      Toast.show("问诊时长已延长");
    } catch {
      Toast.show("延长问诊失败");
    }
  }

  async function handleComplete(): Promise<void> {
    if (!consultId) {
      Toast.show("缺少问诊编号");
      return;
    }
    try {
      await completeConsult(consultId);
      Toast.show("问诊已完成");
      navigate("/consult/list", { replace: true });
    } catch {
      Toast.show("完成问诊失败");
    }
  }

  /** 上传图片（模拟）。 */
  function handleSendImage(): void {
    // 实际项目中应调用文件选择器，此处发送占位图片
    sendMessage("IMAGE", "https://via.placeholder.com/300x200?text=report");
  }

  return (
    <div className="consult-chat-page">
      {/* 顶部渐变背景栏 */}
      <header className="chat-page-header">
        <div className="chat-page-nav">
          <span className="chat-page-back" onClick={() => navigate(-1)}>‹ 返回</span>
          <strong>图文问诊</strong>
          <span className="chat-page-record" onClick={() => navigate("/consult/list")}>记录</span>
        </div>

        {/* 医生信息 */}
        <div className="chat-page-doctor">
          <div className="chat-page-avatar">医</div>
          <div>
            <div className="chat-page-doctor-name">{doctorName}</div>
            <div className="chat-page-doctor-sub">医生 · 在线</div>
          </div>
        </div>

        {/* 状态卡 */}
        <div className="chat-page-status-card">
          <span>问诊中</span>
          <span>剩余 {formatRemaining(remainingSeconds)}</span>
        </div>
      </header>

      {/* 温馨提示 */}
      <div className="chat-page-notice">
        温馨提示：图文问诊仅支持文字和图片沟通，如出现胸痛、呼吸困难等紧急情况，请立即线下就医。
      </div>

      {/* 聊天主体 */}
      <ConsultChat
        doctorName={doctorName}
        remainingSeconds={remainingSeconds}
        messages={messages}
        textMessage={textMessage}
        canSend={Boolean(consultId)}
        onTextChange={setTextMessage}
        onSendText={() => sendMessage("TEXT", textMessage)}
        onSendImage={handleSendImage}
      />

      {/* 底部操作栏 */}
      <div className="chat-page-actions">
        <button className="chat-action-btn" onClick={handleExtend}>延长问诊</button>
        <button className="chat-action-btn chat-action-btn--primary" onClick={handleComplete}>结束问诊</button>
      </div>
    </div>
  );
}

function formatRemaining(totalSeconds: number): string {
  if (totalSeconds <= 0) return "00:00";
  const m = Math.floor(totalSeconds / 60);
  const s = totalSeconds % 60;
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}

function resolveConsultWsUrl(consultId: number): string {
  const token = useSessionStore.getState().token;
  const apiBaseUrl = import.meta.env.VITE_WS_BASE_URL ?? import.meta.env.VITE_API_BASE_URL ?? defaultWsBaseUrl();
  const baseUrl = apiBaseUrl.startsWith("http") || apiBaseUrl.startsWith("ws") ? apiBaseUrl : `${window.location.origin}${apiBaseUrl}`;
  const wsUrl = new URL(`/ws/consult/${consultId}`, baseUrl);
  wsUrl.protocol = wsUrl.protocol === "https:" ? "wss:" : "ws:";
  wsUrl.searchParams.set("token", token.replace(`${AUTHORIZATION_TOKEN_PREFIX} `, ""));
  return wsUrl.toString();
}

function defaultWsBaseUrl(): string {
  if (window.location.hostname === "localhost" && window.location.port === "13300") {
    return "http://localhost:19000";
  }
  return "/api";
}
