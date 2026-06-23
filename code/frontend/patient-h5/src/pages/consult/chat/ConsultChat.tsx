import { Button, Image, Input, List, Space, Tag, TextArea } from "antd-mobile";
import { formatRemainingTime } from "../../../utils/time";

export type ConsultMessageItem = {
  id?: number;
  content: string;
  contentType: "TEXT" | "IMAGE";
  senderType?: string;
};

type ConsultChatProps = {
  remainingSeconds: number;
  messages: ConsultMessageItem[];
  textMessage: string;
  imageUrl: string;
  canSend: boolean;
  onTextChange: (value: string) => void;
  onImageUrlChange: (value: string) => void;
  onSendText: () => void;
  onSendImage: () => void;
};

export function ConsultChat({
  remainingSeconds,
  messages,
  textMessage,
  imageUrl,
  canSend,
  onTextChange,
  onImageUrlChange,
  onSendText,
  onSendImage
}: ConsultChatProps) {
  return (
    <Space direction="vertical" block className="chat-stack">
      <div className="chat-header">
        <Tag color="warning">剩余时间 {formatRemainingTime(remainingSeconds)}</Tag>
        <div className="section-description">已接入问诊 WebSocket 实时沟通</div>
      </div>
      <List className="chat-list">
        {messages.map((message, index) => (
          <List.Item key={message.id ?? index} description={message.senderType ?? message.contentType}>
            {message.contentType === "IMAGE" ? <Image src={message.content} fit="cover" className="chat-image" /> : message.content}
          </List.Item>
        ))}
      </List>
      <TextArea rows={3} value={textMessage} onChange={onTextChange} placeholder="请输入文字消息" disabled={!canSend} />
      <Space block className="chat-image-input">
        <Input value={imageUrl} onChange={onImageUrlChange} placeholder="请输入图片 URL" disabled={!canSend} />
        <Button onClick={onSendImage} disabled={!canSend}>发送图片</Button>
      </Space>
      <Button color="primary" block onClick={onSendText} disabled={!canSend}>发送文字</Button>
    </Space>
  );
}
