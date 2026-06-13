import { Card, Col, Form, Input, InputNumber, Modal, Row, Select, Spin, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { fetchDoctors } from '@/api/doctor';
import { createDoctor, createDoctorSchedule, updateDoctorStatus } from '@/api/modules';
import PageHero from '@/components/PageHero';
import DoctorList, { type DoctorRecord } from '@/pages/doctor/components/DoctorList';

function DoctorPage() {
  const [doctors, setDoctors] = useState<DoctorRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [doctorOpen, setDoctorOpen] = useState(false);
  const [scheduleOpen, setScheduleOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState<DoctorRecord | null>(null);
  const [doctorForm] = Form.useForm();
  const [scheduleForm] = Form.useForm();

  const refreshDoctors = () => {
    setLoading(true);
    fetchDoctors()
      .then(setDoctors)
      .catch(() => {
        message.warning('医生服务暂不可用，请稍后重试');
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    fetchDoctors()
      .then((records) => {
        if (!ignore) {
          setDoctors(records);
        }
      })
      .catch(() => {
        message.warning('医生服务暂不可用，请稍后重试');
      })
      .finally(() => {
        if (!ignore) {
          setLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  const handleCreateDoctor = async () => {
    const values = await doctorForm.validateFields();
    setSubmitting(true);
    try {
      await createDoctor(values);
      message.success('医生创建成功');
      setDoctorOpen(false);
      doctorForm.resetFields();
      refreshDoctors();
    } catch {
      message.warning('医生创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (doctor: DoctorRecord) => {
    const nextStatus = doctor.status.includes('停') ? 'ONLINE' : 'OFFLINE';
    setSubmitting(true);
    try {
      await updateDoctorStatus(doctor.key, nextStatus);
      message.success('医生状态已更新');
      refreshDoctors();
    } catch {
      message.warning('医生状态更新失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleOpenSchedule = (doctor: DoctorRecord) => {
    setSelectedDoctor(doctor);
    scheduleForm.setFieldsValue({
      doctorId: Number(doctor.key),
      slot: doctor.schedule || '2026-06-13 上午',
      scheduleDate: '2026-06-13',
      timeSlot: '上午',
      totalNumber: 30,
      remainNumber: 30,
    });
    setScheduleOpen(true);
  };

  const handleCreateSchedule = async () => {
    const values = await scheduleForm.validateFields();
    setSubmitting(true);
    try {
      await createDoctorSchedule(values);
      message.success('排班创建成功');
      setScheduleOpen(false);
      scheduleForm.resetFields();
      refreshDoctors();
    } catch {
      message.warning('排班创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <div className="page-shell">
        <PageHero eyebrow="医生管理" title="医生名录与排班概览" description="医生模块采用独立可复用的 DoctorList 组件。" badgeText="DoctorList 已抽离" />
        <Row gutter={[18, 18]}>
          {[
            ['医生总数', String(doctors.length), '来自后端医生接口'],
            ['接诊医生', String(doctors.filter((doctor) => doctor.status.includes('接诊')).length), '按接诊状态实时统计'],
            ['今日接诊量', String(doctors.reduce((sum, doctor) => sum + doctor.patientCount, 0)), '汇总当前医生接诊数'],
          ].map(([label, value, hint]) => (
            <Col key={label} xs={24} md={8}>
              <Card className="metric-card" bordered={false}>
                <span className="metric-card__label">{label}</span>
                <strong className="metric-card__value">{value}</strong>
                <Typography.Text className="metric-card__hint">{hint}</Typography.Text>
              </Card>
            </Col>
          ))}
        </Row>
        <Spin spinning={loading || submitting}>
          <DoctorList
            doctors={doctors}
            onCreateDoctor={() => setDoctorOpen(true)}
            onCreateSchedule={handleOpenSchedule}
            onToggleStatus={handleToggleStatus}
          />
        </Spin>
      </div>
      <Modal
        title="新增医生"
        open={doctorOpen}
        confirmLoading={submitting}
        onOk={handleCreateDoctor}
        onCancel={() => setDoctorOpen(false)}
        destroyOnClose
      >
        <Form form={doctorForm} layout="vertical" className="module-form" initialValues={{ consultStatus: 'ONLINE', status: '接诊中', consultFee: 30 }}>
          <Form.Item name="name" label="医生姓名" rules={[{ required: true, message: '请输入医生姓名' }]}>
            <Input placeholder="请输入医生姓名" />
          </Form.Item>
          <Form.Item name="title" label="职称" rules={[{ required: true, message: '请输入职称' }]}>
            <Input placeholder="例如：主治医师" />
          </Form.Item>
          <Form.Item name="department" label="所属科室" rules={[{ required: true, message: '请输入所属科室' }]}>
            <Input placeholder="例如：全科" />
          </Form.Item>
          <Form.Item name="specialty" label="擅长方向">
            <Input placeholder="例如：慢病复诊" />
          </Form.Item>
          <Form.Item name="consultFee" label="问诊费用">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="consultStatus" label="接诊状态">
            <Select
              options={[
                { label: '在线', value: 'ONLINE' },
                { label: '离线', value: 'OFFLINE' },
              ]}
            />
          </Form.Item>
          <Form.Item name="schedule" label="排班描述">
            <Input placeholder="例如：2026-06-13 上午" />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title={`创建排班${selectedDoctor ? `：${selectedDoctor.name}` : ''}`}
        open={scheduleOpen}
        confirmLoading={submitting}
        onOk={handleCreateSchedule}
        onCancel={() => setScheduleOpen(false)}
        destroyOnClose
      >
        <Form form={scheduleForm} layout="vertical" className="module-form">
          <Form.Item name="doctorId" label="医生编号" rules={[{ required: true, message: '请输入医生编号' }]}>
            <InputNumber min={1} className="module-form__number" />
          </Form.Item>
          <Form.Item name="slot" label="出诊时段" rules={[{ required: true, message: '请输入出诊时段' }]}>
            <Input placeholder="例如：2026-06-13 上午" />
          </Form.Item>
          <Form.Item name="scheduleDate" label="排班日期">
            <Input placeholder="例如：2026-06-13" />
          </Form.Item>
          <Form.Item name="timeSlot" label="时间段">
            <Input placeholder="例如：上午" />
          </Form.Item>
          <Form.Item name="totalNumber" label="总号源">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="remainNumber" label="剩余号源">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default DoctorPage;
