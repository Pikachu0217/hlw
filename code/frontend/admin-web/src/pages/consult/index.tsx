import { Button, Form, Input, InputNumber, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import {
  acceptConsult,
  completeConsult,
  createConsult,
  extendConsult,
  fetchConsults,
} from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface ConsultRecord {
  key: string;
  consultNo: string;
  patientName: string;
  doctorName: string;
  channel: string;
  status: string;
  updatedAt: string;
}

const columns = (
  onAccept: (record: ConsultRecord) => void,
  onComplete: (record: ConsultRecord) => void,
  onExtend: (record: ConsultRecord) => void,
): ColumnsType<ConsultRecord> => [
  { title: '咨询单号', dataIndex: 'consultNo' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '接诊医生', dataIndex: 'doctorName' },
  { title: '渠道', dataIndex: 'channel' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={statusColor(value)}>{value}</Tag> },
  { title: '最近更新时间', dataIndex: 'updatedAt' },
  {
    title: '操作',
    key: 'actions',
    render: (_, record) => (
      <Space wrap>
        <Button type="link" onClick={() => onAccept(record)} disabled={record.status !== '待接单'}>
          接单
        </Button>
        <Button type="link" onClick={() => onExtend(record)} disabled={!['咨询中', '已延长'].includes(record.status)}>
          延长
        </Button>
        <Button type="link" onClick={() => onComplete(record)} disabled={record.status === '已完成' || record.status === '已取消'}>
          完成
        </Button>
      </Space>
    ),
  },
];

function ConsultPage() {
  const { records, loading, refresh } = useModuleRecords(fetchConsults, '问诊');
  const [form] = Form.useForm();
  const [acceptForm] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [acceptOpen, setAcceptOpen] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<ConsultRecord | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const waitingCount = records.filter((record) => record.status.includes('待')).length;
  const activeCount = records.filter((record) => ['咨询中', '已延长'].includes(record.status)).length;

  async function handleCreate() {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await createConsult(values);
      message.success('问诊单创建成功');
      setOpen(false);
      form.resetFields();
      refresh();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '问诊单创建失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  function openAcceptModal(record: ConsultRecord) {
    setCurrentRecord(record);
    acceptForm.setFieldsValue({ doctorId: 1 });
    setAcceptOpen(true);
  }

  async function handleAccept() {
    if (!currentRecord) {
      return;
    }
    const values = await acceptForm.validateFields();
    setSubmitting(true);
    try {
      await acceptConsult(currentRecord.key, values);
      message.success('问诊接单成功');
      setAcceptOpen(false);
      setCurrentRecord(null);
      acceptForm.resetFields();
      refresh();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '问诊接单失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleComplete(record: ConsultRecord) {
    setSubmitting(true);
    try {
      await completeConsult(record.key);
      message.success('问诊已完成');
      refresh();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '问诊完成失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleExtend(record: ConsultRecord) {
    setSubmitting(true);
    try {
      await extendConsult(record.key);
      message.success('问诊已延长');
      refresh();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '问诊延长失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <ModulePage<ConsultRecord>
        eyebrow="咨询中心"
        title="咨询单流转看板"
        description="统一处理问诊创建、医生接单、服务延长和完成归档。"
        badgeText="问诊工作流已接入数据库"
        metrics={[
          { label: '问诊单', value: String(records.length), hint: '来自后端问诊接口' },
          { label: '待接单', value: String(waitingCount), hint: '按状态字段实时统计' },
          { label: '进行中', value: String(activeCount), hint: '覆盖图文与视频渠道' },
        ]}
        columns={columns(openAcceptModal, handleComplete, handleExtend)}
        dataSource={records}
        loading={loading || submitting}
        tableTitle="咨询单列表"
        searchPlaceholder="搜索咨询单、患者、医生"
        getSearchText={(record) => `${record.consultNo} ${record.patientName} ${record.doctorName} ${record.channel}`}
        onCreate={() => setOpen(true)}
      />
      <Modal
        title="新增问诊单"
        open={open}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form
          form={form}
          layout="vertical"
          className="module-form"
          initialValues={{
            patientName: '赵晓岚',
            doctorName: '陈知衡',
            type: 'IMAGE_TEXT',
            channel: '图文',
            feeAmount: 39.9,
          }}
        >
          <Form.Item name="patientName" label="患者姓名" rules={[{ required: true, message: '请输入患者姓名' }]}>
            <Input placeholder="请输入患者姓名" />
          </Form.Item>
          <Form.Item name="doctorName" label="医生姓名" rules={[{ required: true, message: '请输入医生姓名' }]}>
            <Input placeholder="请输入医生姓名" />
          </Form.Item>
          <Form.Item name="type" label="问诊类型">
            <Select
              options={[
                { label: '图文问诊', value: 'IMAGE_TEXT' },
                { label: '视频问诊', value: 'VIDEO' },
                { label: '复诊问诊', value: 'FOLLOW_UP' },
              ]}
            />
          </Form.Item>
          <Form.Item name="channel" label="问诊渠道">
            <Select
              options={[
                { label: '图文', value: '图文' },
                { label: '视频', value: '视频' },
              ]}
            />
          </Form.Item>
          <Form.Item name="feeAmount" label="问诊费用">
            <InputNumber min={0} precision={2} className="module-form__number" />
          </Form.Item>
          <Form.Item name="chiefComplaint" label="主诉">
            <Input.TextArea rows={3} placeholder="请输入患者主诉" />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title="接单问诊"
        open={acceptOpen}
        confirmLoading={submitting}
        onOk={handleAccept}
        onCancel={() => {
          setAcceptOpen(false);
          setCurrentRecord(null);
        }}
        destroyOnClose
      >
        <Form form={acceptForm} layout="vertical" className="module-form">
          <Form.Item label="咨询单号">
            <Input value={currentRecord?.consultNo} disabled />
          </Form.Item>
          <Form.Item name="doctorId" label="接诊医生编号">
            <InputNumber min={1} className="module-form__number" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

function statusColor(status: string): string {
  switch (status) {
    case '待接单':
      return 'gold';
    case '咨询中':
      return 'blue';
    case '已延长':
      return 'cyan';
    case '已完成':
      return 'green';
    case '已取消':
    case '已超时':
      return 'red';
    default:
      return 'processing';
  }
}

export default ConsultPage;
