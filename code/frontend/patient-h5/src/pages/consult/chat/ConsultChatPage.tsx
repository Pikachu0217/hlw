import { useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { Toast } from "antd-mobile";
import { fetchConsultMessages, type ConsultMessageItem } from "../../../app/api";
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
    socketRef.current?.close();
    socketRef.current = null;

    if (!consultId) {
      return undefined;
    }

    const socket = new WebSocket(resolveConsultWsUrl(consultId));
    socketRef.current = socket;
    socket.onmessage = (event) => {
      const payload = JSON.parse(event.data) as ConsultMessageItem;
      setMessages((items) => [...items, payload]);
    };
    socket.onerror = () => Toast.show("问诊连接异常，请稍后重试");

    return () => socket.close();
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

  return (
    <SectionCard title="问诊聊天" description="支持文字和图片 URL 的实时问诊沟通。">
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
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "/api";
  const baseUrl = apiBaseUrl.startsWith("http") ? apiBaseUrl : `${window.location.origin}${apiBaseUrl}`;
  const wsUrl = new URL(`/ws/consult/${consultId}`, baseUrl);
  wsUrl.protocol = wsUrl.protocol === "https:" ? "wss:" : "ws:";
  wsUrl.searchParams.set("token", token.replace(`${AUTHORIZATION_TOKEN_PREFIX} `, ""));
  return wsUrl.toString();
}
