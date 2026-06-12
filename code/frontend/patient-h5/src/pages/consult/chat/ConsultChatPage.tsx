import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { fetchConsultMessages, type ConsultMessageItem } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";
import { ConsultChat } from "./ConsultChat";

export function ConsultChatPage() {
  const [searchParams] = useSearchParams();
  const consultId = Number(searchParams.get("consultId") ?? 0);
  const [messages, setMessages] = useState<ConsultMessageItem[]>([]);
  const remainingSeconds = Number(searchParams.get("remainingSeconds") ?? 0);

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

  return (
    <SectionCard title="问诊聊天" description="显示剩余问诊时间和当前消息列表。">
      <ConsultChat remainingSeconds={remainingSeconds} messages={messages} />
    </SectionCard>
  );
}
