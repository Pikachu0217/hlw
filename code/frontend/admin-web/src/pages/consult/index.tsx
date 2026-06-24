import { App, Button, Card, Empty, Input, List, Space, Tag, Typography } from 'antd';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  acceptConsult,
  completeConsult,
  extendConsult,
  fetchConsultMessages,
  fetchDoctorConsultWorkbench,
  type ConsultMessageRecord,
  type DoctorConsultWorkbenchRecord,
} from '@/api/modules';
import { AUTHORIZATION_TOKEN_PREFIX } from '@/api/auth-header';
import PageHero from '@/components/PageHero';
import { readAuthToken } from '@/utils/auth-storage';

export interface ConsultRecord {
  id: number;
  consultNo: string;
  patientName: string;
  doctorName: string;
  channel: string;
  status: string;
  updatedAt: string;
}

const ACTIVE_STATUS = ['待接单', '咨询中', '已延长'];

function ConsultPage() {
  const { message } = App.useApp();
  const [records, setRecords] = useState<DoctorConsultWorkbenchRecord[]>([]);
  const [messages, setMessages] = useState<ConsultMessageRecord[]>([]);
  const [activeConsultId, setActiveConsultId] = useState<number>();
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [textMessage, setTextMessage] = useState('');
  const [imageUrl, setImageUrl] = useState('');
  const socketRef = useRef<WebSocket | null>(null);

  const activeRecord = useMemo(
    () => records.find((record) => record.consultId === activeConsultId),
    [activeConsultId, records],
  );
  const canSend = Boolean(activeRecord && activeRecord.status !== '已完成' && activeRecord.status !== '已取消');

  const loadWorkbench = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchDoctorConsultWorkbench();
      setRecords(data);
      setActiveConsultId((current) => current ?? data[0]?.consultId);
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '医生咨询工作台加载失败');
    } finally {
      setLoading(false);
    }
  }, []);

  const loadMessages = useCallback(async (consultId: number) => {
    try {
      const data = await fetchConsultMessages(consultId);
      setMessages(data);
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '问诊消息加载失败');
    }
  }, []);

  useEffect(() => {
    loadWorkbench();
  }, [loadWorkbench]);

  useEffect(() => {
    if (!activeConsultId) {
      setMessages([]);
      return;
    }
    loadMessages(activeConsultId);
  }, [activeConsultId, loadMessages]);

  useEffect(() => {
    let cancelled = false;
    let connectTimer: number | undefined;

    socketRef.current?.close();
    socketRef.current = null;

    if (!activeConsultId) {
      return undefined;
    }

    connectTimer = window.setTimeout(() => {
      if (cancelled) {
        return;
      }

      const socket = new WebSocket(resolveConsultWsUrl(activeConsultId));
      socketRef.current = socket;
      socket.onmessage = (event) => {
        const payload = JSON.parse(event.data) as ConsultMessageRecord;
        setMessages((items) => [...items, payload]);
        loadWorkbench();
      };
      socket.onerror = () => {
        if (!cancelled) {
          message.warning('问诊 IM 连接异常，请刷新后重试');
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
  }, [activeConsultId, loadWorkbench]);

  async function handleAccept() {
    if (!activeRecord) {
      return;
    }
    setSubmitting(true);
    try {
      await acceptConsult(activeRecord.consultId, {});
      message.success('问诊接单成功');
      await loadWorkbench();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '问诊接单失败');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleExtend() {
    if (!activeRecord) {
      return;
    }
    setSubmitting(true);
    try {
      await extendConsult(activeRecord.consultId);
      message.success('问诊已延长');
      await loadWorkbench();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '问诊延长失败');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleComplete() {
    if (!activeRecord) {
      return;
    }
    setSubmitting(true);
    try {
      await completeConsult(activeRecord.consultId);
      message.success('问诊已完成');
      await loadWorkbench();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '问诊完成失败');
    } finally {
      setSubmitting(false);
    }
  }

  function sendMessage(contentType: 'TEXT' | 'IMAGE', content: string) {
    const trimmedContent = content.trim();
    if (!trimmedContent) {
      message.warning(contentType === 'TEXT' ? '请输入消息内容' : '请输入图片地址');
      return;
    }
    if (!socketRef.current || socketRef.current.readyState !== WebSocket.OPEN) {
      message.warning('问诊 IM 尚未连接，请稍后重试');
      return;
    }
    socketRef.current.send(JSON.stringify({ contentType, content: trimmedContent }));
    if (contentType === 'TEXT') {
      setTextMessage('');
    } else {
      setImageUrl('');
    }
  }

  return (
    <div className="page-shell consult-workbench">
      <PageHero
        eyebrow="咨询中心"
        title="医生患者 IM 工作台"
        description="当前登录医生可查看待接单、咨询中和已延长的问诊患者，并进行文字或图片 URL 沟通。"
        badgeText="医生工作台已接入实时消息"
      />
      <div className="consult-workbench__layout">
        <Card className="consult-workbench__patients" bordered={false} loading={loading}>
          <Typography.Title level={4} className="consult-workbench__title">咨询患者</Typography.Title>
          <List
            dataSource={records}
            locale={{ emptyText: <Empty description="暂无待处理咨询" /> }}
            renderItem={(record) => (
              <List.Item
                className={record.consultId === activeConsultId ? 'consult-workbench__patient consult-workbench__patient--active' : 'consult-workbench__patient'}
                onClick={() => setActiveConsultId(record.consultId)}
              >
                <div className="consult-workbench__patient-main">
                  <strong>{record.patientName}</strong>
                  <span>{record.consultNo}</span>
                  <small>{record.lastMessage || '暂无消息'}</small>
                </div>
                <Tag color={statusColor(record.status)}>{record.status}</Tag>
              </List.Item>
            )}
          />
        </Card>
        <Card className="consult-workbench__chat" bordered={false}>
          {activeRecord ? (
            <>
              <div className="consult-workbench__chat-header">
                <div>
                  <Typography.Title level={4} className="consult-workbench__title">{activeRecord.patientName}</Typography.Title>
                  <Typography.Text type="secondary">{activeRecord.channel} · 剩余 {formatRemaining(activeRecord.remainingSeconds)}</Typography.Text>
                </div>
                <Space wrap>
                  <Button onClick={handleAccept} disabled={activeRecord.status !== '待接单'} loading={submitting}>接单</Button>
                  <Button onClick={handleExtend} disabled={!['咨询中', '已延长'].includes(activeRecord.status)} loading={submitting}>延长</Button>
                  <Button type="primary" danger onClick={handleComplete} disabled={!ACTIVE_STATUS.includes(activeRecord.status)} loading={submitting}>完成</Button>
                </Space>
              </div>
              <div className="consult-workbench__messages">
                {messages.map((item, index) => (
                  <div key={item.id ?? `${item.createTime}-${index}`} className={item.senderType === 'DOCTOR' ? 'consult-message consult-message--doctor' : 'consult-message'}>
                    <span className="consult-message__sender">{item.senderType === 'DOCTOR' ? '医生' : '患者'}</span>
                    {item.contentType === 'IMAGE' ? <img src={item.content} alt="问诊图片" className="consult-message__image" /> : <span className="consult-message__bubble">{item.content}</span>}
                  </div>
                ))}
              </div>
              <div className="consult-workbench__composer">
                <Input.TextArea rows={3} value={textMessage} onChange={(event) => setTextMessage(event.target.value)} placeholder="请输入文字消息" disabled={!canSend} />
                <Space.Compact className="consult-workbench__image-input">
                  <Input value={imageUrl} onChange={(event) => setImageUrl(event.target.value)} placeholder="请输入图片 URL" disabled={!canSend} />
                  <Button onClick={() => sendMessage('IMAGE', imageUrl)} disabled={!canSend}>发送图片</Button>
                </Space.Compact>
                <Button type="primary" onClick={() => sendMessage('TEXT', textMessage)} disabled={!canSend}>发送文字</Button>
              </div>
            </>
          ) : <Empty description="请选择咨询患者" />}
        </Card>
      </div>
    </div>
  );
}

function statusColor(status: string): string {
  if (status === '待接单') {
    return 'gold';
  }
  if (status === '咨询中') {
    return 'blue';
  }
  if (status === '已延长') {
    return 'cyan';
  }
  return 'default';
}

function formatRemaining(seconds?: number): string {
  const safeSeconds = Math.max(seconds ?? 0, 0);
  const minutes = Math.floor(safeSeconds / 60);
  const restSeconds = safeSeconds % 60;
  return `${minutes}分${restSeconds}秒`;
}

function resolveConsultWsUrl(consultId: number): string {
  const token = readAuthToken();
  const apiBaseUrl = import.meta.env.VITE_WS_BASE_URL ?? import.meta.env.VITE_API_BASE_URL ?? defaultWsBaseUrl();
  const baseUrl = apiBaseUrl.startsWith('http') || apiBaseUrl.startsWith('ws') ? apiBaseUrl : `${window.location.origin}${apiBaseUrl}`;
  const wsUrl = new URL(`/ws/consult/${consultId}`, baseUrl);
  wsUrl.protocol = wsUrl.protocol === 'https:' ? 'wss:' : 'ws:';
  wsUrl.searchParams.set('token', token.replace(`${AUTHORIZATION_TOKEN_PREFIX} `, ''));
  return wsUrl.toString();
}

function defaultWsBaseUrl(): string {
  if (window.location.hostname === 'localhost' && window.location.port === '13200') {
    return 'http://localhost:19000';
  }
  return '/api';
}

export default ConsultPage;
