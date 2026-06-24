import { Button, Form, Input, InputNumber, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import {
  approvePrescription,
  createPrescription,
  fetchPrescriptions,
  rejectPrescription,
  submitPrescription,
} from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';
import { prescriptionStatusColor as statusColor } from '@/utils/status-color';

export interface PrescriptionRecord {
  id: number;
  prescriptionNo: string;
  patientName: string;
  doctorName: string;
  drugCount: number;
  issuedAt: string;
  status: string;
}

type AuditMode = 'approve' | 'reject';

const columns = (
  onSubmit: (record: PrescriptionRecord) => void,
  onAudit: (record: PrescriptionRecord, mode: AuditMode) => void,
): ColumnsType<PrescriptionRecord> => [
  { title: '处方编号', dataIndex: 'prescriptionNo' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '开方医生', dataIndex: 'doctorName' },
  { title: '药品数', dataIndex: 'drugCount' },
  { title: '开立时间', dataIndex: 'issuedAt' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={statusColor(value)}>{value}</Tag> },
  {
    title: '操作',
    key: 'actions',
    render: (_, record) => (
      <Space wrap>
        <Button type="link" onClick={() => onSubmit(record)} disabled={record.status !== '草稿'}>
          提交
        </Button>
        <Button type="link" onClick={() => onAudit(record, 'approve')} disabled={record.status !== '待审方'}>
          通过
        </Button>
        <Button type="link" danger onClick={() => onAudit(record, 'reject')} disabled={record.status !== '待审方'}>
          驳回
        </Button>
      </Space>
    ),
  },
];

function PrescriptionPage() {
  const { records, loading, refresh } = useModuleRecords(fetchPrescriptions, '处方');
  const [createForm] = Form.useForm();
  const [auditForm] = Form.useForm();
  const [createOpen, setCreateOpen] = useState(false);
  const [auditOpen, setAuditOpen] = useState(false);
  const [auditMode, setAuditMode] = useState<AuditMode>('approve');
  const [currentRecord, setCurrentRecord] = useState<PrescriptionRecord | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const auditCount = records.filter((record) => record.status.includes('审')).length;
  const draftCount = records.filter((record) => record.status === '草稿').length;

  async function handleCreate() {
    const values = await createForm.validateFields();
    setSubmitting(true);
    try {
      await createPrescription({ ...values, drugIds: [values.drugId] });
      message.success('处方草稿创建成功');
      setCreateOpen(false);
      createForm.resetFields();
      refresh();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '处方创建失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleSubmit(record: PrescriptionRecord) {
    setSubmitting(true);
    try {
      await submitPrescription(record.id);
      message.success('处方提交成功');
      refresh();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '处方提交失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  function openAuditModal(record: PrescriptionRecord, mode: AuditMode) {
    setCurrentRecord(record);
    setAuditMode(mode);
    auditForm.setFieldsValue({ pharmacistId: 1, remark: mode === 'approve' ? '审核通过' : '用药信息需补充' });
    setAuditOpen(true);
  }

  async function handleAudit() {
    if (!currentRecord) {
      return;
    }
    const values = await auditForm.validateFields();
    setSubmitting(true);
    try {
      if (auditMode === 'approve') {
        await approvePrescription(currentRecord.id, values);
        message.success('处方审核通过');
      } else {
        await rejectPrescription(currentRecord.id, values);
        message.success('处方已驳回');
      }
      setAuditOpen(false);
      setCurrentRecord(null);
      auditForm.resetFields();
      refresh();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '处方审核操作失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <ModulePage<PrescriptionRecord>
        eyebrow="处方中心"
        title="处方流转与审方准备"
        description="以处方草稿、提审、审方通过和驳回为核心管理药品处方流转。"
        badgeText="处方工作流已接入数据库"
        metrics={[
          { label: '处方数', value: String(records.length), hint: '来自后端处方接口' },
          { label: '草稿', value: String(draftCount), hint: '可继续提交审方' },
          { label: '待审方', value: String(auditCount), hint: '按处方状态实时统计' },
        ]}
        columns={columns(handleSubmit, openAuditModal)}
        dataSource={records}
        loading={loading || submitting}
        tableTitle="处方列表"
        searchPlaceholder="搜索处方编号、患者或医生"
        getSearchText={(record) => `${record.prescriptionNo} ${record.patientName} ${record.doctorName} ${record.status}`}
        onCreate={() => setCreateOpen(true)}
      />
      <Modal
        title="新增处方草稿"
        open={createOpen}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setCreateOpen(false)}
        destroyOnClose
      >
        <Form
          form={createForm}
          layout="vertical"
          className="module-form"
          initialValues={{
            patientName: '赵晓岚',
            doctorName: '陈知衡',
            consultId: 1,
            patientId: 1,
            doctorId: 1,
            drugId: 1,
          }}
        >
          <Form.Item name="patientName" label="患者姓名" rules={[{ required: true, message: '请输入患者姓名' }]}>
            <Input placeholder="请输入患者姓名" />
          </Form.Item>
          <Form.Item name="doctorName" label="开方医生" rules={[{ required: true, message: '请输入开方医生' }]}>
            <Input placeholder="请输入开方医生" />
          </Form.Item>
          <Form.Item name="consultId" label="问诊编号">
            <InputNumber min={1} className="module-form__number" />
          </Form.Item>
          <Form.Item name="drugId" label="药品编号" rules={[{ required: true, message: '请输入药品编号' }]}>
            <InputNumber min={1} className="module-form__number" />
          </Form.Item>
          <Form.Item name="drugCount" label="药品数量">
            <InputNumber min={1} className="module-form__number" />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title={auditMode === 'approve' ? '审核通过处方' : '驳回处方'}
        open={auditOpen}
        confirmLoading={submitting}
        onOk={handleAudit}
        onCancel={() => {
          setAuditOpen(false);
          setCurrentRecord(null);
        }}
        destroyOnClose
      >
        <Form form={auditForm} layout="vertical" className="module-form">
          <Form.Item label="处方编号">
            <Input value={currentRecord?.prescriptionNo} disabled />
          </Form.Item>
          {auditMode === 'approve' ? (
            <Form.Item name="pharmacistId" label="审核药师编号">
              <InputNumber min={1} className="module-form__number" />
            </Form.Item>
          ) : null}
          <Form.Item name="remark" label={auditMode === 'approve' ? '审核备注' : '驳回原因'}>
            <Input.TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default PrescriptionPage;
