import { useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Button, Space, Toast } from "antd-mobile";
import { completeConsult, extendConsult, fetchConsultMessages, type ConsultMessageItem } from "../../../app/api";
import { AUTHORIZATION_TOKEN_PREFIX } from "../../../app/auth-header";
import { SectionCard } from "../../../components/SectionCard";
import { useSessionStore } from "../../../store/sessionStore";
import { ConsultChat } from "./ConsultChat";

export function ConsultChatPage() {
  const [searchParams] = useSearchParams();
  const consultId = Number(searchParams.get("consultId") ?? 0);
  const [messages, setMessages] = useState<ConsultMessageItem[]>([]);
  const [textMessage, setTextMessage] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const remainingSeconds = Number(searchParams.get("remainingSeconds") ?? 0);
  const socketRef = useRef<WebSocket | null>(null);

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
        }
      });

    return () => {
      ignore = true;
    };
  }, [consultId]);

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
        const payload = JSON.parse(event.data) as ConsultMessageItem;
        setMessages((items) => [...items, payload]);
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

  function sendMessage(contentType: "TEXT" | "IMAGE", content: string): void {
    const trimmedContent = content.trim();
    if (!trimmedContent) {
      Toast.show(contentType === "TEXT" ? "请输入消息内容" : "请输入图片地址");
      return;
    }
    if (!socketRef.current || socketRef.current.readyState !== WebSocket.OPEN) {
      Toast.show("问诊连接中，请稍后重试");
      return;
    }
    socketRef.current.send(JSON.stringify({ contentType, content: trimmedContent }));
    if (contentType === "TEXT") {
      setTextMessage("");
    } else {
      setImageUrl("");
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
    } catch {
      Toast.show("完成问诊失败");
    }
  }

  return (
    <SectionCard title="问诊聊天" description="支持文字和图片 URL 的实时问诊沟通。">
      <Space block className="consult-action-bar">
        <Button size="small" onClick={handleExtend}>
          延长问诊
        </Button>
        <Button size="small" color="primary" onClick={handleComplete}>
          完成问诊
        </Button>
      </Space>
      <ConsultChat
        remainingSeconds={remainingSeconds}
        messages={messages}
        textMessage={textMessage}
        imageUrl={imageUrl}
        canSend={Boolean(consultId)}
        onTextChange={setTextMessage}
        onImageUrlChange={setImageUrl}
        onSendText={() => sendMessage("TEXT", textMessage)}
        onSendImage={() => sendMessage("IMAGE", imageUrl)}
      />
    </SectionCard>
  );
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
