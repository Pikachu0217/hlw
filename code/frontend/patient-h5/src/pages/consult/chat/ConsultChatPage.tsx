import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchConsultMessages, type ConsultMessageItem } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";
import { ConsultChat } from "./ConsultChat";

const fallbackMessages: ConsultMessageItem[] = [
  { id: 1, content: "哪里不舒服", contentType: "TEXT" },
  { id: 2, content: "孩子从昨晚开始发烧", contentType: "TEXT" }
];

export function ConsultChatPage() {
  const [searchParams] = useSearchParams();
  const consultId = Number(searchParams.get("consultId") ?? 1);
  const [messages, setMessages] = useState<ConsultMessageItem[]>(fallbackMessages);

  useEffect(() => {
    let ignore = false;

    fetchConsultMessages(consultId)
      .then((records) => {
        if (!ignore) {
          setMessages(records);
        }
      })
      .catch(() => {
        console.warn("[consult] 问诊消息服务未连接，使用本地兜底消息");
      });

    return () => {
      ignore = true;
    };
  }, [consultId]);

  return (
    <SectionCard title="问诊聊天" description="显示剩余问诊时间和当前消息列表。">
      <ConsultChat remainingSeconds={300} messages={messages} />
    </SectionCard>
  );
}
