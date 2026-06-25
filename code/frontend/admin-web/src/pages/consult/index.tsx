import { App, Button, Card, Empty, Input, List, Segmented, Space, Tag, Typography } from 'antd';
import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  acceptConsult,
  buildConsultImageUrl,
  completeConsult,
  extendConsult,
  fetchConsultMessages,
  fetchDoctorConsultWorkbench,
  normalizeConsultImageContent,
  rejectConsult,
  uploadConsultImage,
  type ConsultMessageRecord,
  type DoctorConsultWorkbenchRecord,
} from '@/api/modules';
import { AUTHORIZATION_TOKEN_PREFIX } from '@/api/auth-header';
import PageHero from '@/components/PageHero';
import { readAuthToken } from '@/utils/auth-storage';
import { consultStatusColor as statusColor } from '@/utils/status-color';

export interface ConsultRecord {
  id: number;
  consultNo: string;
  patientName: string;
  doctorName: string;
  channel: string;
  status: string;
  updatedAt: string;
}

type ConsultViewStatus = '待接诊' | '接诊中' | '已完成';

const VIEW_STATUS_OPTIONS: ConsultViewStatus[] = ['待接诊', '接诊中', '已完成'];
const IN_CONSULT_STATUS = ['咨询中', '已延长'];

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
  const imageInputRef = useRef<HTMLInputElement | null>(null);

  const activeRecord = useMemo(
    () => records.find((record) => record.consultId === activeConsultId),
    [activeConsultId, records],
  );
  const [viewStatus, setViewStatus] = useState<ConsultViewStatus>('待接诊');
  const filteredRecords = useMemo(
    () => records.filter((record) => toViewStatus(record.status) === viewStatus),
    [records, viewStatus],
  );
  const statusCounts = useMemo(
    () => VIEW_STATUS_OPTIONS.reduce<Record<ConsultViewStatus, number>>((result, status) => {
      result[status] = records.filter((record) => toViewStatus(record.status) === status).length;
      return result;
    }, {
      待接诊: 0,
      接诊中: 0,
      已完成: 0,
    }),
    [records],
  );
  const activePatientName = activeRecord ? displayPatientName(activeRecord) : '';
  const canSend = Boolean(activeRecord && IN_CONSULT_STATUS.includes(activeRecord.status));

  const loadWorkbench = useCallback(async () => {
    setLoading(true);
    try {
      const data = await fetchDoctorConsultWorkbench();
      setRecords(data);
      setActiveConsultId((current) => current && data.some((record) => record.consultId === current) ? current : data[0]?.consultId);
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
    if (!activeRecord || toViewStatus(activeRecord.status) !== viewStatus) {
      setActiveConsultId(filteredRecords[0]?.consultId);
    }
  }, [activeRecord, filteredRecords, viewStatus]);

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
        setMessages((items) => [
          ...items,
          {
            ...payload,
            content: payload.contentType === 'IMAGE' ? normalizeConsultImageContent(payload.content) : payload.content,
          },
        ]);
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

  async function handleReject() {
    if (!activeRecord) {
      return;
    }
    setSubmitting(true);
    try {
      await rejectConsult(activeRecord.consultId);
      message.success('问诊已拒诊');
      setActiveConsultId(undefined);
      await loadWorkbench();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '问诊拒诊失败');
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

  async function handleUploadImage(file: File | undefined) {
    if (!file) {
      return;
    }
    if (!file.type.startsWith('image/')) {
      message.warning('请选择图片文件');
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      message.warning('图片不能超过 5MB');
      return;
    }
    try {
      const result = await uploadConsultImage(file);
      sendMessage('IMAGE', buildConsultImageUrl(result.objectName));
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '图片上传失败');
    }
  }

  return (
    <div className="page-shell consult-workbench">
      <PageHero
        eyebrow="咨询中心"
        title="医生患者 IM 工作台"
        description="当前登录医生可按待接诊、接诊中、已完成查看问诊患者，并进行文字或图片沟通。"
        badgeText="医生工作台已接入实时消息"
      />
      <div className="consult-workbench__layout">
        <Card className="consult-workbench__patients" bordered={false} loading={loading}>
          <div className="consult-workbench__patients-header">
            <Typography.Title level={4} className="consult-workbench__title">咨询患者</Typography.Title>
            <Segmented
              className="consult-workbench__status-tabs"
              value={viewStatus}
              options={VIEW_STATUS_OPTIONS.map((status) => ({
                label: `${status} ${statusCounts[status]}`,
                value: status,
              }))}
              onChange={(value) => setViewStatus(value as ConsultViewStatus)}
            />
          </div>
          <List
            dataSource={filteredRecords}
            locale={{ emptyText: <Empty description={`暂无${viewStatus}问诊`} /> }}
            renderItem={(record) => (
              <List.Item
                className={record.consultId === activeConsultId ? 'consult-workbench__patient consult-workbench__patient--active' : 'consult-workbench__patient'}
                onClick={() => setActiveConsultId(record.consultId)}
              >
                <div className="consult-workbench__patient-main">
                  <strong>{displayPatientName(record)}</strong>
                  <span>{record.consultNo}</span>
                  <small>{record.lastMessage || '暂无消息'}</small>
                </div>
                <Tag color={statusColor(record.status)}>{displayConsultStatus(record.status)}</Tag>
              </List.Item>
            )}
          />
        </Card>
        <Card className="consult-workbench__chat" bordered={false}>
          {activeRecord ? (
            <>
              <div className="consult-workbench__chat-header">
                <div>
                  <Typography.Title level={4} className="consult-workbench__title">{activePatientName}</Typography.Title>
                  <Typography.Text type="secondary">{activeRecord.channel} · {displayConsultStatus(activeRecord.status)} · 剩余 {formatRemaining(activeRecord.remainingSeconds)}</Typography.Text>
                </div>
                <Space wrap>
                  <Button onClick={handleAccept} disabled={activeRecord.status !== '待接单'} loading={submitting}>接单</Button>
                  <Button danger onClick={handleReject} disabled={activeRecord.status !== '待接单'} loading={submitting}>拒诊</Button>
                  <Button onClick={handleExtend} disabled={!IN_CONSULT_STATUS.includes(activeRecord.status)} loading={submitting}>延长</Button>
                  <Button type="primary" danger onClick={handleComplete} disabled={!IN_CONSULT_STATUS.includes(activeRecord.status)} loading={submitting}>完成</Button>
                </Space>
              </div>
              <div className="consult-workbench__chief-complaint">
                <span>问题描述</span>
                <strong>{activeRecord.chiefComplaint || activeRecord.lastMessage || '患者暂未填写问题描述'}</strong>
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
                <input
                  ref={imageInputRef}
                  className="consult-workbench__file-input"
                  type="file"
                  accept="image/*"
                  onChange={(event) => {
                    const file = event.target.files?.[0];
                    event.target.value = '';
                    void handleUploadImage(file);
                  }}
                />
                <Button onClick={() => imageInputRef.current?.click()} disabled={!canSend}>上传图片</Button>
                <Button type="primary" onClick={() => sendMessage('TEXT', textMessage)} disabled={!canSend}>发送文字</Button>
              </div>
            </>
          ) : <Empty description="请选择咨询患者" />}
        </Card>
      </div>
    </div>
  );
}

function formatRemaining(seconds?: number): string {
  const safeSeconds = Math.max(seconds ?? 0, 0);
  const minutes = Math.floor(safeSeconds / 60);
  const restSeconds = safeSeconds % 60;
  return `${minutes}分${restSeconds}秒`;
}

function displayPatientName(record: DoctorConsultWorkbenchRecord): string {
  const patientName = record.patientName?.trim();
  if (patientName) {
    return patientName;
  }
  return record.patientId ? `患者${record.patientId}` : '未知患者';
}

function toViewStatus(status: string): ConsultViewStatus {
  if (status === '待接单') {
    return '待接诊';
  }
  if (IN_CONSULT_STATUS.includes(status)) {
    return '接诊中';
  }
  return '已完成';
}

function displayConsultStatus(status: string): string {
  if (status === '待接单') {
    return '待接诊';
  }
  if (status === '已延长') {
    return '接诊中';
  }
  return status;
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
