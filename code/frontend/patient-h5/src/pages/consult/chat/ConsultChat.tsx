import { useRef, useEffect } from "react";

export type ConsultMessageItem = {
  id?: number;
  content: string;
  contentType: "TEXT" | "IMAGE";
  senderType?: string;
};

type ConsultChatProps = {
  /** 医生姓名。 */
  doctorName: string;
  /** 剩余秒数。 */
  remainingSeconds: number;
  /** 消息列表。 */
  messages: ConsultMessageItem[];
  /** 输入框文本。 */
  textMessage: string;
  /** 是否允许发送。 */
  canSend: boolean;
  /** 输入框文本变化回调。 */
  onTextChange: (value: string) => void;
  /** 发送文本消息。 */
  onSendText: () => void;
  /** 上传/发送图片。 */
  onSendImage: (file: File) => void;
};

/** 格式化剩余时间 mm:ss */
function formatRemaining(totalSeconds: number): string {
  if (totalSeconds <= 0) return "00:00";
  const m = Math.floor(totalSeconds / 60);
  const s = totalSeconds % 60;
  return `${String(m).padStart(2, "0")}:${String(s).padStart(2, "0")}`;
}

/** 判断消息是否为患者发送（右对齐蓝色气泡）。 */
function isPatientMessage(msg: ConsultMessageItem): boolean {
  return msg.senderType === "PATIENT";
}

/** 消息气泡组件。 */
function MessageBubble({ message }: { message: ConsultMessageItem }) {
  const isPatient = isPatientMessage(message);
  const bubbleClass = isPatient ? "chat-bubble chat-bubble--right" : "chat-bubble chat-bubble--left";
  const avatarText = isPatient ? "我" : "医";
  const avatarClass = isPatient ? "chat-avatar chat-avatar--patient" : "chat-avatar chat-avatar--doctor";

  return (
    <div className={`chat-msg ${isPatient ? "chat-msg--right" : "chat-msg--left"}`}>
      {!isPatient && <div className={avatarClass}>{avatarText}</div>}
      <div className={bubbleClass}>
        {message.contentType === "IMAGE" ? (
          <img className="chat-img" src={message.content} alt="问诊图片" />
        ) : (
          message.content
        )}
      </div>
      {isPatient && <div className={avatarClass}>{avatarText}</div>}
    </div>
  );
}

/**
 * 类微信问诊聊天组件。
 * 包含消息列表 + 快捷回复 + 输入区。
 */
export function ConsultChat({
  doctorName,
  remainingSeconds,
  messages,
  textMessage,
  canSend,
  onTextChange,
  onSendText,
  onSendImage
}: ConsultChatProps) {
  const listRef = useRef<HTMLDivElement>(null);
  const imageInputRef = useRef<HTMLInputElement>(null);

  // 新消息时自动滚动到底部
  useEffect(() => {
    if (listRef.current) {
      listRef.current.scrollTop = listRef.current.scrollHeight;
    }
  }, [messages]);

  /** 患者快捷输入语。 */
  const quickReplies = [
    "我有发热、咳嗽的症状",
    "持续3天了",
    "没有药物过敏史",
    "需要开点药吗？"
  ];

  function handleQuickReply(text: string): void {
    if (!canSend) {
      return;
    }
    onTextChange(text);
  }

  function handleKeyDown(e: React.KeyboardEvent<HTMLTextAreaElement>): void {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      onSendText();
    }
  }

  function handleImageChange(event: React.ChangeEvent<HTMLInputElement>): void {
    const file = event.target.files?.[0];
    event.target.value = "";
    if (!file) {
      return;
    }
    onSendImage(file);
  }

  return (
    <div className="chat-container">
      {/* --- 顶部状态栏 --- */}
      <div className="chat-status-bar">
        <span className="chat-status-tag">问诊中</span>
        <span className="chat-status-time">剩余 {formatRemaining(remainingSeconds)}</span>
      </div>

      {/* --- 消息列表 --- */}
      <div className="chat-messages" ref={listRef}>
        <div className="chat-time">今天</div>
        {messages.map((msg, index) => (
          <MessageBubble key={msg.id ?? index} message={msg} />
        ))}
      </div>

      {/* --- 底部输入区 --- */}
      <div className="chat-composer">
        {/* 快捷回复 */}
        <div className="chat-quick-replies">
          {quickReplies.map((text) => (
            <span key={text} className="chat-quick-tag" onClick={() => handleQuickReply(text)}>
              {text}
            </span>
          ))}
        </div>

        {/* 工具行 */}
        <div className="chat-tools">
          <input
            ref={imageInputRef}
            className="chat-file-input"
            type="file"
            accept="image/*"
            onChange={handleImageChange}
          />
          <button className="chat-tool-btn" onClick={() => imageInputRef.current?.click()} disabled={!canSend}>
            上传图片
          </button>
        </div>

        {/* 输入行 */}
        <div className="chat-input-row">
          <textarea
            className="chat-textarea"
            rows={2}
            value={textMessage}
            onChange={(e) => onTextChange(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder={canSend ? "请输入回复内容，仅支持文字和图片..." : "医生接诊后可发送消息"}
            disabled={!canSend}
          />
          <button className="chat-send-btn" onClick={onSendText} disabled={!canSend || !textMessage.trim()}>
            发送
          </button>
        </div>
      </div>
    </div>
  );
}
