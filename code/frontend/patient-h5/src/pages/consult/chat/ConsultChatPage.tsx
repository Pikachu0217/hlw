import { SectionCard } from "../../../components/SectionCard";
import { ConsultChat } from "./ConsultChat";

export function ConsultChatPage() {
  return (
    <SectionCard title="问诊聊天" description="显示剩余问诊时间和当前消息列表。">
      <ConsultChat
        remainingSeconds={300}
        messages={[
          { id: 1, content: "哪里不舒服", contentType: "TEXT" },
          { id: 2, content: "孩子从昨晚开始发烧", contentType: "TEXT" }
        ]}
      />
    </SectionCard>
  );
}
