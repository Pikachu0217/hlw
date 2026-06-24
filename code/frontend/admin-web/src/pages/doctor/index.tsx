import { Col, Form, Input, InputNumber, Modal, Row, Select, Spin, message } from 'antd';
import { useMemo, useState } from 'react';
import {
  bindDoctorDepartment,
  createDoctorSchedule,
  fetchDepartments,
  fetchDicts,
  fetchDoctorDepartmentBindings,
  fetchDoctors,
  updateDoctorExtension,
  updateDoctorStatus,
} from '@/api/modules';
import { MetricCard } from '@/components/MetricCard';
import PageHero from '@/components/PageHero';
import DoctorList, { type DoctorRecord } from '@/pages/doctor/components/DoctorList';
import type { DepartmentRecord } from '@/pages/doctor/departments';
import { useModuleRecords } from '@/hooks/useModuleRecords';

const DOCTOR_JOB_TITLE_DICT_TYPE = 'doctor_job_title';
const TITLE_FEE_MAP: Record<string, number> = {
  主任医师: 50,
  副主任医师: 20,
  主治医师: 10,
};
const SCHEDULE_TIME_SLOT_OPTIONS = [
  '09:00-09:30',
  '09:30-10:00',
  '10:00-10:30',
  '10:30-11:00',
  '11:00-11:30',
  '13:30-14:00',
  '14:00-14:30',
  '14:30-15:00',
  '15:00-15:30',
  '15:30-16:00',
  '16:00-16:30',
  '16:30-17:00',
  '17:00-17:30',
].map((value) => ({ label: value, value }));

function formatDateValue(date: Date) {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function parseScheduleSlotStart(value: string) {
  const [hour = '0', minute = '0'] = value.split('-')[0].split(':');
  return Number(hour) * 60 + Number(minute);
}

function buildScheduleTimeSlotOptions(scheduleDate?: string) {
  const today = formatDateValue(new Date());
  if (scheduleDate !== today) {
    return SCHEDULE_TIME_SLOT_OPTIONS;
  }
  const now = new Date();
  const currentSlotStart = now.getHours() * 60 + Math.floor(now.getMinutes() / 30) * 30;
  return SCHEDULE_TIME_SLOT_OPTIONS.filter((option) => parseScheduleSlotStart(option.value) >= currentSlotStart);
}

function buildScheduleDateOptions() {
  return Array.from({ length: 14 }).map((_, index) => {
    const date = new Date();
    date.setDate(date.getDate() + index);
    const value = formatDateValue(date);
    return { label: value, value };
  }).filter((option) => buildScheduleTimeSlotOptions(option.value).length > 0);
}

function DoctorPage() {
  const { records: doctors, loading, refresh: refreshDoctors } = useModuleRecords(fetchDoctors, '医生');
  const { records: departments } = useModuleRecords(fetchDepartments, '科室');
  const { records: dicts } = useModuleRecords(fetchDicts, '字典');
  const { records: doctorDepartmentBindings, refresh: refreshDoctorDepartmentBindings } = useModuleRecords(fetchDoctorDepartmentBindings, '医生科室绑定');
  const [doctorOpen, setDoctorOpen] = useState(false);
  const [scheduleOpen, setScheduleOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [selectedDoctor, setSelectedDoctor] = useState<DoctorRecord | null>(null);
  const [editingDoctor, setEditingDoctor] = useState<DoctorRecord | null>(null);
  const [doctorForm] = Form.useForm();
  const [scheduleForm] = Form.useForm();
  const selectedScheduleDate = Form.useWatch('scheduleDate', scheduleForm);
  const scheduleDateOptions = useMemo(() => buildScheduleDateOptions(), []);
  const scheduleTimeSlotOptions = useMemo(
    () => buildScheduleTimeSlotOptions(selectedScheduleDate ?? scheduleDateOptions[0]?.value),
    [scheduleDateOptions, selectedScheduleDate],
  );
  const jobTitleOptions = useMemo(() => {
    const options = dicts
      .filter((dict) => dict.dictType === DOCTOR_JOB_TITLE_DICT_TYPE)
      .sort((left, right) => (left.dictSort ?? 0) - (right.dictSort ?? 0))
      .map((dict) => ({ label: dict.dictLabel, value: dict.dictLabel }));
    return options.length > 0
      ? options
      : Object.keys(TITLE_FEE_MAP).map((title) => ({ label: title, value: title }));
  }, [dicts]);
  const departmentOptions = useMemo(
    () => departments.map((department) => ({ label: department.name, value: department.name })),
    [departments],
  );
  const selectedDoctorBindingOptions = useMemo(
    () =>
      doctorDepartmentBindings
        .filter((binding) => binding.doctorId === selectedDoctor?.doctorId)
        .map((binding) => ({
          label: binding.label || `${binding.doctorName}(${binding.departmentName})`,
          value: `${binding.doctorId}:${binding.deptId}`,
        })),
    [doctorDepartmentBindings, selectedDoctor?.doctorId],
  );

  const handleOpenEditDoctor = (doctor: DoctorRecord) => {
    setEditingDoctor(doctor);
    doctorForm.setFieldsValue({
      userId: doctor.userId ?? doctor.id,
      name: doctor.name,
      title: doctor.title,
      department: doctor.department ? doctor.department.split(/[、,，]/).filter(Boolean) : [],
      specialty: doctor.specialty,
      consultFee: Number(doctor.consultFee ?? 0),
      consultStatus: doctor.consultStatus,
      status: doctor.status,
      schedule: doctor.schedule,
    });
    setDoctorOpen(true);
  };

  const handleSaveDoctorExtension = async () => {
    const values = await doctorForm.validateFields();
    const payload = {
      ...values,
      name: editingDoctor?.name,
      department: Array.isArray(values.department) ? values.department.join('、') : values.department,
      consultFee: undefined,
    };
    setSubmitting(true);
    try {
      const savedDoctor = await updateDoctorExtension(values.userId, payload);
      const doctorId = editingDoctor?.doctorId ?? savedDoctor.doctorId;
      if (doctorId && Array.isArray(values.department)) {
        await Promise.all(
          values.department
            .map((departmentName: string) => departments.find((department) => department.name === departmentName))
            .filter(Boolean)
            .map((department: DepartmentRecord) =>
              bindDoctorDepartment(doctorId, {
                deptId: department.deptId ?? department.id ?? 0,
                free: false,
                appointmentFee: TITLE_FEE_MAP[values.title] ?? 0,
              }),
            ),
        );
      }
      message.success('医生扩展属性已更新');
      setDoctorOpen(false);
      setEditingDoctor(null);
      doctorForm.resetFields();
      refreshDoctors();
      refreshDoctorDepartmentBindings();
    } catch {
      message.warning('医生扩展属性更新失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleToggleStatus = async (doctor: DoctorRecord) => {
    const nextStatus = doctor.consultStatus === 'OFFLINE' ? 'ONLINE' : 'OFFLINE';
    setSubmitting(true);
    try {
      await updateDoctorStatus(doctor.id, nextStatus);
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
    const firstBinding = doctorDepartmentBindings.find((binding) => binding.doctorId === doctor.doctorId);
    const scheduleDate = scheduleDateOptions[0]?.value;
    const timeSlot = buildScheduleTimeSlotOptions(scheduleDate)[0]?.value;
    scheduleForm.setFieldsValue({
      bindingKey: firstBinding ? `${firstBinding.doctorId}:${firstBinding.deptId}` : undefined,
      slot: scheduleDate && timeSlot ? `${scheduleDate} ${timeSlot}` : doctor.schedule,
      scheduleDate,
      timeSlot,
      totalNumber: 30,
      remainNumber: 30,
    });
    setScheduleOpen(true);
  };

  const handleScheduleDateChange = (value: string) => {
    const nextTimeSlot = buildScheduleTimeSlotOptions(value)[0]?.value;
    scheduleForm.setFieldsValue({ timeSlot: nextTimeSlot });
  };

  const handleCreateSchedule = async () => {
    const values = await scheduleForm.validateFields();
    const [doctorId, deptId] = String(values.bindingKey).split(':').map(Number);
    const payload = {
      ...values,
      doctorId,
      deptId,
      slot: `${values.scheduleDate} ${values.timeSlot}`,
    };
    delete payload.bindingKey;
    setSubmitting(true);
    try {
      await createDoctorSchedule(payload);
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
        <PageHero eyebrow="医疗资源" title="医生资源与排班概览" description="将已有医生档案纳入线上资源池，并维护展示信息、接诊状态和排班。" badgeText="资源扩展信息" />
        <Row gutter={[18, 18]}>
          {[
            { title: '医生总数', value: String(doctors.length), note: '来自后端医生接口' },
            { title: '接诊医生', value: String(doctors.filter((doctor) => doctor.consultStatus === 'ONLINE').length), note: '按接诊状态实时统计' },
            { title: '今日接诊量', value: String(doctors.reduce((sum, doctor) => sum + doctor.patientCount, 0)), note: '汇总当前医生接诊数' },
          ].map((item) => (
            <Col key={item.title} xs={24} md={8}>
              <MetricCard title={item.title} value={item.value} note={item.note} />
            </Col>
          ))}
        </Row>
        <Spin spinning={loading || submitting}>
          <DoctorList
            doctors={doctors}
            onEditDoctor={handleOpenEditDoctor}
            onCreateSchedule={handleOpenSchedule}
            onToggleStatus={handleToggleStatus}
          />
        </Spin>
      </div>
      <Modal
        title={editingDoctor ? `编辑医生扩展属性：${editingDoctor.name}` : '编辑医生扩展属性'}
        open={doctorOpen}
        confirmLoading={submitting}
        onOk={handleSaveDoctorExtension}
        onCancel={() => {
          setDoctorOpen(false);
          setEditingDoctor(null);
        }}
        destroyOnClose
      >
        <Form form={doctorForm} layout="vertical" className="module-form" initialValues={{ consultStatus: 'ONLINE', status: '接诊中', consultFee: 0 }}>
          <Form.Item name="userId" label="医生账号编号" rules={[{ required: true, message: '请输入医生账号编号' }]}>
            <Input disabled />
          </Form.Item>
          <Form.Item name="name" label="线上展示姓名" rules={[{ required: true, message: '请输入线上展示姓名' }]}>
            <Input disabled />
          </Form.Item>
          <Form.Item name="title" label="职称">
            <Select
              placeholder="请选择职称"
              options={jobTitleOptions}
              onChange={(value) => doctorForm.setFieldValue('consultFee', TITLE_FEE_MAP[value] ?? 0)}
            />
          </Form.Item>
          <Form.Item name="department" label="线上科室">
            <Select mode="multiple" placeholder="请选择线上科室" options={departmentOptions} />
          </Form.Item>
          <Form.Item name="specialty" label="擅长方向">
            <Input placeholder="例如：慢病复诊" />
          </Form.Item>
          <Form.Item name="consultFee" label="问诊费用">
            <InputNumber min={0} disabled className="module-form__number" />
          </Form.Item>
          <Form.Item name="consultStatus" label="接诊状态">
            <Select
              options={[
                { label: '在线', value: 'ONLINE' },
                { label: '忙碌', value: 'BUSY' },
                { label: '离线', value: 'OFFLINE' },
              ]}
            />
          </Form.Item>
          <Form.Item name="schedule" label="线上排班描述">
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
          <Form.Item name="bindingKey" label="医生姓名（科室）" rules={[{ required: true, message: '请选择医生科室关系' }]}>
            <Select placeholder="请选择医生科室关系" options={selectedDoctorBindingOptions} />
          </Form.Item>
          <Form.Item name="scheduleDate" label="排班日期" rules={[{ required: true, message: '请选择排班日期' }]}>
            <Select placeholder="请选择排班日期" options={scheduleDateOptions} onChange={handleScheduleDateChange} />
          </Form.Item>
          <Form.Item name="timeSlot" label="时间段" rules={[{ required: true, message: '请选择时间段' }]}>
            <Select placeholder="请选择时间段" options={scheduleTimeSlotOptions} />
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
