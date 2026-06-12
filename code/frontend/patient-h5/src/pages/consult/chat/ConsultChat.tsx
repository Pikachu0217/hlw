import { List, Space, Tag } from "antd-mobile";
import { formatRemainingTime } from "../../../utils/time";

type ConsultMessageItem = {
  id: number;
  content: string;
  contentType: "TEXT" | "IMAGE";
};

type ConsultChatProps = {
  remainingSeconds: number;
  messages: ConsultMessageItem[];
};

export function ConsultChat({ remainingSeconds, messages }: ConsultChatProps) {
  return (
    <Space direction="vertical" block style={{ "--gap": "12px" }}>
      <div className="chat-header">
        <Tag color="warning">剩余时间 {formatRemainingTime(remainingSeconds)}</Tag>
        <div className="section-description">WebSocket 地址预留为 /ws/consult/{"{consultId}"}</div>
      </div>
      <List className="chat-list">
        {messages.map((message) => (
          <List.Item key={message.id} description={message.contentType}>
            {message.content}
          </List.Item>
        ))}
      </List>
    </Space>
  );
}
