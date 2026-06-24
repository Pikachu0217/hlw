import {
  Button,
  Card,
  Col,
  Form,
  Input,
  InputNumber,
  Modal,
  Row,
  Select,
  Space,
  Table,
  Tag,
  Typography,
  message,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState, useEffect, useCallback } from 'react';
import {
  checkInAppointment,
  createAppointment,
  createReleaseConfig,
  fetchAppointments,
  fetchNumberSources,
  fetchNumberSourceStats,
  grabAppointment,
  lockNumberSource,
  payAppointment,
  type NumberSourceRecord,
  type NumberSourceStatsRecord,
} from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface AppointmentRecord {
  id: number;
  appointmentNo: string;
  patientName: string;
  doctorName: string;
  clinicTime: string;
  source: string;
  status: string;
}

const appointmentColumns = (
  onPay: (record: AppointmentRecord) => void,
  onCheckIn: (record: AppointmentRecord) => void,
  onGrab: (record: AppointmentRecord) => void,
): ColumnsType<AppointmentRecord> => [
  { title: '预约单号', dataIndex: 'appointmentNo' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '医生', dataIndex: 'doctorName' },
  { title: '门诊时间', dataIndex: 'clinicTime' },
  { title: '来源', dataIndex: 'source' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={appointmentStatusColor(value)}>{value}</Tag> },
  {
    title: '操作',
    key: 'actions',
    render: (_, record) => (
      <Space wrap>
        <Button type="link" onClick={() => onPay(record)} disabled={record.status !== '待支付'}>
          支付
        </Button>
        <Button type="link" onClick={() => onCheckIn(record)} disabled={record.status !== '已支付'}>
          签到
        </Button>
        <Button
          type="link"
          onClick={() => onGrab(record)}
          disabled={record.status === '已完成' || record.status === '已取消'}
        >
          抢单
        </Button>
      </Space>
    ),
  },
];

const numberSourceColumns = (
  onLock: (record: NumberSourceRecord) => void,
  availableCount: number,
): ColumnsType<NumberSourceRecord> => [
  { title: '号源编号', dataIndex: 'id' },
  { title: '排班编号', dataIndex: 'scheduleId' },
  { title: '序号', dataIndex: 'numberSeq' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={numberSourceStatusColor(value)}>{value}</Tag> },
  {
    title: '操作',
    key: 'actions',
    render: (_, record) => (
      <Button type="link" onClick={() => onLock(record)} disabled={availableCount <= 0}>
        锁定排班号源
      </Button>
    ),
  },
];

function AppointmentPage() {
  const { records, loading, refresh } = useModuleRecords(fetchAppointments, '预约');
  const { records: numberSources, loading: sourceLoading, refresh: refreshNumberSources } = useModuleRecords(fetchNumberSources, '号源');
  const [appointmentForm] = Form.useForm();
  const [grabForm] = Form.useForm();
  const [releaseForm] = Form.useForm();
  const [appointmentOpen, setAppointmentOpen] = useState(false);
  const [grabOpen, setGrabOpen] = useState(false);
  const [releaseOpen, setReleaseOpen] = useState(false);
  const [currentAppointment, setCurrentAppointment] = useState<AppointmentRecord | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [stats, setStats] = useState<NumberSourceStatsRecord | null>(null);
  const DEFAULT_SCHEDULE_ID = 1;
  const currentScheduleId = DEFAULT_SCHEDULE_ID;

  const checkedInCount = records.filter((record) => record.status.includes('签到')).length;
  const paidCount = records.filter((record) => ['已支付', '已签到', '已完成'].includes(record.status)).length;
  const availableCount = stats?.availableCount ?? 0;
  const lockedCount = stats?.lockedCount ?? 0;

  const columns = appointmentColumns(handlePay, handleCheckIn, openGrabModal);

  const sourceColumns = numberSourceColumns(handleLockNumberSource, availableCount);

  /** 加载号源统计信息。 */
  const loadStats = useCallback(async () => {
    try {
      const result = await fetchNumberSourceStats(currentScheduleId);
      setStats(result);
    } catch {
      // 统计加载失败不影响主流程
    }
  }, [currentScheduleId]);

  useEffect(() => {
    void loadStats();
  }, [loadStats]);

  async function handleCreateAppointment() {
    const values = await appointmentForm.validateFields();
    setSubmitting(true);
    try {
      await createAppointment(values);
      message.success('预约单创建成功');
      setAppointmentOpen(false);
      appointmentForm.resetFields();
      refresh();
      refreshNumberSources();
      void loadStats();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '预约单创建失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  async function handlePay(record: AppointmentRecord) {
    try {
      await checkAction(() => payAppointment(record.id), '预约支付成功');
    } catch {
      return;
    }
    refresh();
  }

  async function handleCheckIn(record: AppointmentRecord) {
    try {
      await checkAction(() => checkInAppointment(record.id), '预约签到成功');
    } catch {
      return;
    }
    refresh();
  }

  function openGrabModal(record: AppointmentRecord) {
    setCurrentAppointment(record);
    grabForm.setFieldsValue({ doctorId: 1 });
    setGrabOpen(true);
  }

  async function handleGrabAppointment() {
    if (!currentAppointment) {
      return;
    }
    const values = await grabForm.validateFields();
    setSubmitting(true);
    try {
      await grabAppointment(currentAppointment.id, values);
      message.success('预约抢单成功');
      setGrabOpen(false);
      setCurrentAppointment(null);
      grabForm.resetFields();
      refresh();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '预约抢单失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  async function handleLockNumberSource(record: NumberSourceRecord) {
    try {
      await checkAction(() => lockNumberSource(record.scheduleId), `排班 ${record.scheduleId} 号源已锁定`);
    } catch {
      return;
    }
    refreshNumberSources();
    void loadStats();
  }

  async function handleCreateReleaseConfig() {
    const values = await releaseForm.validateFields();
    setSubmitting(true);
    try {
      await createReleaseConfig(values);
      message.success('放号配置创建成功');
      setReleaseOpen(false);
      releaseForm.resetFields();
      refreshNumberSources();
      void loadStats();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '放号配置创建失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  async function checkAction(action: () => Promise<unknown>, successMessage: string) {
    setSubmitting(true);
    try {
      await action();
      message.success(successMessage);
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '操作失败，请稍后重试');
      throw error;
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <ModulePage<AppointmentRecord>
        eyebrow="预约管理"
        title="门诊预约排期"
        description="围绕预约创建、支付签到、便民门诊抢单和号源锁定形成一体化操作台。"
        badgeText="预约工作流已接入数据库"
        metrics={[
          { label: '今日预约', value: String(records.length), hint: '来自后端预约接口' },
          { label: '已支付', value: String(paidCount), hint: '支付后可继续签到或接诊' },
          { label: '已签到', value: String(checkedInCount), hint: '按接口状态实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading || submitting}
        tableTitle="预约列表"
        searchPlaceholder="搜索预约单、患者、医生"
        getSearchText={(record) => `${record.appointmentNo} ${record.patientName} ${record.doctorName} ${record.source}`}
        onCreate={() => setAppointmentOpen(true)}
      />
      <Row gutter={[18, 18]}>
        <Col xs={24} xl={15}>
          <Card className="console-card" bordered={false}>
            <div className="console-card__toolbar">
              <div>
                <Typography.Title level={4} className="console-card__title">
                  号源池
                </Typography.Title>
                <Typography.Text className="console-card__subtitle">维护当前排班号源并执行锁号动作。</Typography.Text>
              </div>
              <Space wrap>
                <Tag color="blue">可用 {availableCount}</Tag>
                <Tag color="gold">锁定 {lockedCount}</Tag>
              </Space>
            </div>
            <Table<NumberSourceRecord>
              rowKey="id"
              columns={sourceColumns}
              dataSource={numberSources}
              loading={sourceLoading || submitting}
              pagination={false}
            />
          </Card>
        </Col>
        <Col xs={24} xl={9}>
          <Card className="console-card appointment-side-card" bordered={false}>
            <div className="appointment-side-card__header">
              <Typography.Title level={4} className="console-card__title">
                放号配置
              </Typography.Title>
              <Typography.Text className="console-card__subtitle">基于当前排班维护放号时间与放号数量。</Typography.Text>
            </div>
            <div className="appointment-side-card__metrics">
              <div className="appointment-side-card__metric">
                <span>默认排班</span>
                <strong>{currentScheduleId}</strong>
              </div>
              <div className="appointment-side-card__metric">
                <span>可锁号源</span>
                <strong>{availableCount}</strong>
              </div>
            </div>
            <Button type="primary" block onClick={() => setReleaseOpen(true)}>
              新增放号配置
            </Button>
          </Card>
        </Col>
      </Row>
      <Modal
        title="新增预约单"
        open={appointmentOpen}
        confirmLoading={submitting}
        onOk={handleCreateAppointment}
        onCancel={() => setAppointmentOpen(false)}
        destroyOnClose
      >
        <Form
          form={appointmentForm}
          layout="vertical"
          className="module-form"
          initialValues={{
            patientName: '赵晓岚',
            doctorName: '陈知衡',
            source: '小程序',
            timeSlot: '2026-06-13 上午',
            scheduleId: currentScheduleId,
            feeAmount: 30,
            appointmentType: '普通门诊',
          }}
        >
          <Form.Item name="patientName" label="患者姓名" rules={[{ required: true, message: '请输入患者姓名' }]}>
            <Input placeholder="请输入患者姓名" />
          </Form.Item>
          <Form.Item name="doctorName" label="医生姓名" rules={[{ required: true, message: '请输入医生姓名' }]}>
            <Input placeholder="请输入医生姓名" />
          </Form.Item>
          <Form.Item name="timeSlot" label="门诊时间" rules={[{ required: true, message: '请输入门诊时间' }]}>
            <Input placeholder="例如：2026-06-13 上午" />
          </Form.Item>
          <Form.Item name="scheduleId" label="排班编号" rules={[{ required: true, message: '请输入排班编号' }]}>
            <InputNumber min={1} className="module-form__number" />
          </Form.Item>
          <Form.Item name="appointmentType" label="预约类型">
            <Select
              options={[
                { label: '普通门诊', value: '普通门诊' },
                { label: '便民门诊', value: '便民门诊' },
              ]}
            />
          </Form.Item>
          <Form.Item name="source" label="预约来源">
            <Select
              options={[
                { label: '小程序', value: '小程序' },
                { label: '客服代约', value: '客服代约' },
                { label: '管理端', value: '管理端' },
              ]}
            />
          </Form.Item>
          <Form.Item name="feeAmount" label="预约费用">
            <InputNumber min={0} precision={2} className="module-form__number" />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title="抢便民门诊预约单"
        open={grabOpen}
        confirmLoading={submitting}
        onOk={handleGrabAppointment}
        onCancel={() => {
          setGrabOpen(false);
          setCurrentAppointment(null);
        }}
        destroyOnClose
      >
        <Form form={grabForm} layout="vertical" className="module-form">
          <Form.Item label="预约单号">
            <Input value={currentAppointment?.appointmentNo} disabled />
          </Form.Item>
          <Form.Item name="doctorId" label="接诊医生编号" rules={[{ required: true, message: '请输入接诊医生编号' }]}>
            <InputNumber min={1} className="module-form__number" />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title="新增放号配置"
        open={releaseOpen}
        confirmLoading={submitting}
        onOk={handleCreateReleaseConfig}
        onCancel={() => setReleaseOpen(false)}
        destroyOnClose
      >
        <Form
          form={releaseForm}
          layout="vertical"
          className="module-form"
          initialValues={{
            scheduleId: currentScheduleId,
            releaseAt: '2026-06-13 08:00:00',
            releaseCount: 10,
            status: '0',
          }}
        >
          <Form.Item name="scheduleId" label="排班编号" rules={[{ required: true, message: '请输入排班编号' }]}>
            <InputNumber min={1} className="module-form__number" />
          </Form.Item>
          <Form.Item name="releaseAt" label="放号时间" rules={[{ required: true, message: '请输入放号时间' }]}>
            <Input placeholder="例如：2026-06-13 08:00:00" />
          </Form.Item>
          <Form.Item name="releaseCount" label="放号数量" rules={[{ required: true, message: '请输入放号数量' }]}>
            <InputNumber min={1} className="module-form__number" />
          </Form.Item>
          <Form.Item name="status" label="配置状态">
            <Select
              options={[
                { label: '启用', value: '0' },
                { label: '禁用', value: '1' },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

function appointmentStatusColor(status: string): string {
  switch (status) {
    case '待支付':
      return 'gold';
    case '已支付':
      return 'blue';
    case '已签到':
      return 'green';
    case '已接单':
      return 'cyan';
    case '已完成':
      return 'default';
    case '已取消':
      return 'red';
    default:
      return 'processing';
  }
}

function numberSourceStatusColor(status: string): string {
  switch (status) {
    case 'AVAILABLE':
      return 'green';
    case 'LOCKED':
      return 'gold';
    case 'USED':
      return 'blue';
    case 'RELEASED':
      return 'default';
    default:
      return 'processing';
  }
}

export default AppointmentPage;
