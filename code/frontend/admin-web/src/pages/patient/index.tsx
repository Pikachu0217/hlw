import { Button, Form, Input, InputNumber, Modal, Select, Space, Table, Tag, Typography, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  createHealthRecord,
  createPatient,
  fetchHealthRecords,
  fetchPatientDetail,
  fetchPatients,
  updatePatient,
} from '@/api/patient';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface PatientRecord {
  key: string;
  id: number;
  userId?: number;
  patientName: string;
  gender: string;
  age: number;
  riskLevel: string;
  phone: string;
  maskedPhone?: string;
  idCard?: string;
  birthday?: string;
  address?: string;
  lastVisit: string;
  healthRecordCount?: number;
  latestRecordSummary?: string;
  updateTime?: string;
}

export interface HealthRecord {
  key: string;
  id: number;
  patientId: number;
  title: string;
  summary: string;
  allergies?: string;
  history?: string;
  diagnosis?: string;
  remark?: string;
  createTime?: string;
}

const columns: ColumnsType<PatientRecord> = [
  { title: '患者姓名', dataIndex: 'patientName' },
  { title: '性别', dataIndex: 'gender' },
  { title: '年龄', dataIndex: 'age' },
  { title: '风险等级', dataIndex: 'riskLevel', render: (value: string) => <Tag color="orange">{value}</Tag> },
  { title: '联系电话', dataIndex: 'phone' },
  { title: '最近就诊', dataIndex: 'lastVisit' },
  { title: '健康档案数', dataIndex: 'healthRecordCount' },
  {
    title: '最新摘要',
    dataIndex: 'latestRecordSummary',
    render: (value?: string) => <span className="patient-summary-cell">{value || '-'}</span>,
  },
];

function PatientPage() {
  const { records, loading, refresh } = useModuleRecords(fetchPatients, '患者');
  const [patientForm] = Form.useForm();
  const [recordForm] = Form.useForm();
  const [patientModalOpen, setPatientModalOpen] = useState(false);
  const [recordModalOpen, setRecordModalOpen] = useState(false);
  const [patientSubmitting, setPatientSubmitting] = useState(false);
  const [recordSubmitting, setRecordSubmitting] = useState(false);
  const [editingPatient, setEditingPatient] = useState<PatientRecord | null>(null);
  const [selectedPatientId, setSelectedPatientId] = useState<number | null>(null);
  const [patientDetail, setPatientDetail] = useState<PatientRecord | null>(null);
  const [healthRecords, setHealthRecords] = useState<HealthRecord[]>([]);
  const [detailLoading, setDetailLoading] = useState(false);
  const highRiskCount = records.filter((record) => record.riskLevel.includes('高')).length;
  const selectedPatient = useMemo(
    () => records.find((record) => record.id === selectedPatientId) ?? null,
    [records, selectedPatientId],
  );

  const loadPatientDetail = useCallback(async (patientId: number) => {
    setDetailLoading(true);
    try {
      const [detail, recordList] = await Promise.all([fetchPatientDetail(patientId), fetchHealthRecords(patientId)]);
      setPatientDetail(detail);
      setHealthRecords(recordList);
    } catch {
      message.warning('患者详情接口暂不可用，请稍后重试');
    } finally {
      setDetailLoading(false);
    }
  }, []);

  useEffect(() => {
    if (records.length > 0 && selectedPatientId == null) {
      setSelectedPatientId(records[0].id);
    }
  }, [records, selectedPatientId]);

  useEffect(() => {
    if (selectedPatientId != null) {
      loadPatientDetail(selectedPatientId);
    }
  }, [loadPatientDetail, selectedPatientId]);

  const handleOpenCreatePatient = () => {
    setEditingPatient(null);
    patientForm.resetFields();
    patientForm.setFieldsValue({ gender: '女', riskLevel: '低风险', age: 30 });
    setPatientModalOpen(true);
  };

  const handleOpenEditPatient = () => {
    if (!selectedPatient) {
      message.info('请先选择患者');
      return;
    }
    setEditingPatient(selectedPatient);
    patientForm.setFieldsValue({
      patientName: selectedPatient.patientName,
      gender: selectedPatient.gender,
      age: selectedPatient.age,
      phone: selectedPatient.phone,
      riskLevel: selectedPatient.riskLevel,
      idCard: selectedPatient.idCard,
      birthday: selectedPatient.birthday,
      address: selectedPatient.address,
      lastVisit: selectedPatient.lastVisit,
    });
    setPatientModalOpen(true);
  };

  const handleSubmitPatient = async () => {
    const values = await patientForm.validateFields();
    setPatientSubmitting(true);
    try {
      let savedPatient: PatientRecord;
      if (editingPatient) {
        savedPatient = await updatePatient(editingPatient.id, values);
        message.success('患者档案更新成功');
      } else {
        savedPatient = await createPatient(values);
        message.success('患者档案创建成功');
      }
      setPatientModalOpen(false);
      patientForm.resetFields();
      setSelectedPatientId(savedPatient.id);
      loadPatientDetail(savedPatient.id);
      refresh();
    } catch {
      message.warning(editingPatient ? '患者档案更新失败，请稍后重试' : '患者档案创建失败，请稍后重试');
    } finally {
      setPatientSubmitting(false);
    }
  };

  const handleSubmitHealthRecord = async () => {
    if (!selectedPatientId) {
      message.info('请先选择患者');
      return;
    }
    const values = await recordForm.validateFields();
    setRecordSubmitting(true);
    try {
      await createHealthRecord({ patientId: selectedPatientId, ...values });
      message.success('健康档案创建成功');
      setRecordModalOpen(false);
      recordForm.resetFields();
      loadPatientDetail(selectedPatientId);
      refresh();
    } catch {
      message.warning('健康档案创建失败，请稍后重试');
    } finally {
      setRecordSubmitting(false);
    }
  };

  return (
    <>
      <ModulePage<PatientRecord>
        eyebrow="患者中心"
        title="患者档案与健康记录"
        description="承接患者基础资料、风险分层、最近就诊和健康档案摘要，作为问诊、预约和处方的患者主数据入口。"
        metrics={[
          { label: '在管患者', value: String(records.length), hint: '来自后端患者接口' },
          { label: '高风险患者', value: String(highRiskCount), hint: '按风险等级实时统计' },
          { label: '健康档案数', value: String(records.reduce((sum, record) => sum + (record.healthRecordCount ?? 0), 0)), hint: '汇总患者健康记录' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="患者列表"
        searchPlaceholder="搜索患者姓名、电话、身份证或风险等级"
        getSearchText={(record) => `${record.patientName} ${record.phone} ${record.riskLevel} ${record.idCard ?? ''}`}
        onCreate={handleOpenCreatePatient}
      />
      <div className="patient-console">
        <div className="patient-console__side">
          <div className="patient-console__header">
            <div>
              <Typography.Title level={4} className="console-card__title">
                患者详情
              </Typography.Title>
              <Typography.Text className="console-card__subtitle">选中患者后可查看资料与健康档案。</Typography.Text>
            </div>
            <Space wrap>
              <Select
                value={selectedPatientId ?? undefined}
                placeholder="请选择患者"
                className="patient-console__select"
                options={records.map((record) => ({ label: `${record.patientName} · ${record.phone}`, value: record.id }))}
                onChange={(value) => setSelectedPatientId(value)}
              />
              <Button type="link" className="patient-console__link" onClick={handleOpenEditPatient}>
                编辑患者
              </Button>
              <Button type="link" className="patient-console__link" onClick={() => setRecordModalOpen(true)}>
                新增健康档案
              </Button>
            </Space>
          </div>
          <div className="patient-detail-grid">
            <div className="patient-detail-card">
              <span className="patient-detail-card__label">姓名</span>
              <strong>{patientDetail?.patientName ?? '-'}</strong>
              <span>{patientDetail?.maskedPhone ?? patientDetail?.phone ?? '-'}</span>
            </div>
            <div className="patient-detail-card">
              <span className="patient-detail-card__label">风险等级</span>
              <strong>{patientDetail?.riskLevel ?? '-'}</strong>
              <span>最近就诊：{patientDetail?.lastVisit || '-'}</span>
            </div>
            <div className="patient-detail-card">
              <span className="patient-detail-card__label">身份证号</span>
              <strong>{patientDetail?.idCard || '-'}</strong>
              <span>出生日期：{patientDetail?.birthday || '-'}</span>
            </div>
            <div className="patient-detail-card">
              <span className="patient-detail-card__label">联系地址</span>
              <strong>{patientDetail?.address || '-'}</strong>
              <span>更新时间：{patientDetail?.updateTime || '-'}</span>
            </div>
          </div>
        </div>
        <div className="patient-console__records">
          <div className="patient-console__header">
            <div>
              <Typography.Title level={4} className="console-card__title">
                健康档案
              </Typography.Title>
              <Typography.Text className="console-card__subtitle">档案摘要、过敏史与诊断信息统一沉淀在这里。</Typography.Text>
            </div>
            <Tag color="cyan">当前 {healthRecords.length} 条</Tag>
          </div>
          <Table<HealthRecord>
            rowKey="key"
            loading={detailLoading}
            dataSource={healthRecords}
            pagination={false}
            columns={[
              { title: '标题', dataIndex: 'title' },
              { title: '摘要', dataIndex: 'summary', render: (value: string) => <span className="patient-summary-cell">{value}</span> },
              { title: '过敏史', dataIndex: 'allergies', render: (value?: string) => value || '-' },
              { title: '诊断', dataIndex: 'diagnosis', render: (value?: string) => value || '-' },
              { title: '创建时间', dataIndex: 'createTime', width: 180 },
            ]}
          />
        </div>
      </div>
      <Modal
        title={editingPatient ? '编辑患者档案' : '新增患者档案'}
        open={patientModalOpen}
        confirmLoading={patientSubmitting}
        onOk={handleSubmitPatient}
        onCancel={() => setPatientModalOpen(false)}
        destroyOnClose
      >
        <Form form={patientForm} layout="vertical" className="module-form">
          <Form.Item name="patientName" label="患者姓名" rules={[{ required: true, message: '请输入患者姓名' }]}>
            <Input placeholder="请输入患者姓名" />
          </Form.Item>
          <Form.Item name="gender" label="性别" rules={[{ required: true, message: '请选择性别' }]}>
            <Select
              options={[
                { label: '男', value: '男' },
                { label: '女', value: '女' },
              ]}
            />
          </Form.Item>
          <Form.Item name="age" label="年龄" rules={[{ required: true, message: '请输入年龄' }]}>
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="phone" label="联系电话" rules={[{ required: true, message: '请输入联系电话' }]}>
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item name="riskLevel" label="风险等级">
            <Select
              options={[
                { label: '低风险', value: '低风险' },
                { label: '中风险', value: '中风险' },
                { label: '高风险', value: '高风险' },
              ]}
            />
          </Form.Item>
          <Form.Item name="idCard" label="身份证号">
            <Input placeholder="请输入身份证号" />
          </Form.Item>
          <Form.Item name="birthday" label="出生日期">
            <Input placeholder="例如：1992-01-01" />
          </Form.Item>
          <Form.Item name="lastVisit" label="最近就诊日期">
            <Input placeholder="例如：2026-06-13" />
          </Form.Item>
          <Form.Item name="address" label="联系地址">
            <Input.TextArea rows={3} placeholder="请输入联系地址" />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title="新增健康档案"
        open={recordModalOpen}
        confirmLoading={recordSubmitting}
        onOk={handleSubmitHealthRecord}
        onCancel={() => setRecordModalOpen(false)}
        destroyOnClose
      >
        <Form form={recordForm} layout="vertical" className="module-form">
          <Form.Item name="title" label="档案标题" rules={[{ required: true, message: '请输入档案标题' }]}>
            <Input placeholder="请输入档案标题" />
          </Form.Item>
          <Form.Item name="summary" label="档案摘要" rules={[{ required: true, message: '请输入档案摘要' }]}>
            <Input.TextArea rows={3} placeholder="请输入档案摘要" />
          </Form.Item>
          <Form.Item name="allergies" label="过敏史">
            <Input placeholder="请输入过敏史" />
          </Form.Item>
          <Form.Item name="history" label="既往病史">
            <Input.TextArea rows={2} placeholder="请输入既往病史" />
          </Form.Item>
          <Form.Item name="diagnosis" label="诊断信息">
            <Input.TextArea rows={2} placeholder="请输入诊断信息" />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={2} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default PatientPage;
