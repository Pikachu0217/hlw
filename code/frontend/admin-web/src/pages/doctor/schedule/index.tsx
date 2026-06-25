import { Button, DatePicker, Form, Row, Col, Select, Space, Table, Tag, message, Modal, InputNumber } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useMemo, useState } from 'react';
import {
  fetchDepartments,
  fetchDoctors,
  fetchDoctorDepartmentBindings,
  createSchedule,
  updateSchedule,
  deleteSchedule,
  type DoctorRecord,
  type ScheduleQueryParams,
  type ScheduleRecord,
} from '@/api/modules';
import type { DepartmentRecord } from '@/pages/doctor/departments';
import PageHero from '@/components/PageHero';

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

function buildScheduleDateOptions() {
  return Array.from({ length: 14 }).map((_, index) => {
    const date = new Date();
    date.setDate(date.getDate() + index);
    const value = formatDateValue(date);
    return { label: value, value };
  }).filter((option) => {
    const today = formatDateValue(new Date());
    if (option.value !== today) return true;
    const now = new Date();
    const currentSlotStart = now.getHours() * 60 + Math.floor(now.getMinutes() / 30) * 30;
    return SCHEDULE_TIME_SLOT_OPTIONS.some((slot) => parseScheduleSlotStart(slot.value) >= currentSlotStart);
  });
}

function SchedulePage() {
  const [searchForm] = Form.useForm();
  const [form] = Form.useForm();
  const [schedules, setSchedules] = useState<ScheduleRecord[]>([]);
  const [doctors, setDoctors] = useState<DoctorRecord[]>([]);
  const [departments, setDepartments] = useState<DepartmentRecord[]>([]);
  const [doctorDepartmentBindings, setDoctorDepartmentBindings] = useState<{ doctorId: number; deptId: number; label: string }[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<ScheduleRecord | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const scheduleDateOptions = useMemo(() => buildScheduleDateOptions(), []);
  const [selectedScheduleDate, setSelectedScheduleDate] = useState<string>();
  const scheduleTimeSlotOptions = useMemo(() => {
    if (!selectedScheduleDate) return SCHEDULE_TIME_SLOT_OPTIONS;
    const today = formatDateValue(new Date());
    if (selectedScheduleDate !== today) return SCHEDULE_TIME_SLOT_OPTIONS;
    const now = new Date();
    const currentSlotStart = now.getHours() * 60 + Math.floor(now.getMinutes() / 30) * 30;
    return SCHEDULE_TIME_SLOT_OPTIONS.filter((option) => parseScheduleSlotStart(option.value) >= currentSlotStart);
  }, [selectedScheduleDate]);

  useEffect(() => {
    void Promise.all([
      fetchDoctors().then(setDoctors).catch(() => {}),
      fetchDepartments().then(setDepartments).catch(() => {}),
      fetchDoctorDepartmentBindings().then(setDoctorDepartmentBindings).catch(() => {}),
    ]);
    void loadSchedules();
  }, []);

  useEffect(() => {
    if (!modalOpen) {
      setEditingRecord(null);
      form.resetFields();
    }
  }, [modalOpen]);

  /** 选中医生时联动过滤其关联科室，并更新 bindingKey 选项。 */
  const [filteredBindingOptions, setFilteredBindingOptions] = useState<{ label: string; value: string }[]>([]);

  function handleDoctorChange(doctorId: number): void {
    const doctorBindings = doctorDepartmentBindings.filter((b) => b.doctorId === doctorId);
    setFilteredBindingOptions(
      doctorBindings.map((b) => ({ label: b.label, value: `${b.doctorId}:${b.deptId}` })),
    );
    // 清空已选的科室绑定
    form.setFieldValue('bindingKey', undefined);
  }

  async function loadSchedules(params?: ScheduleQueryParams): Promise<void> {
    setLoading(true);
    try {
      const records = await fetchSchedules(params);
      setSchedules(Array.isArray(records) ? records : []);
    } catch {
      message.warning('排班列表加载失败');
      setSchedules([]);
    } finally {
      setLoading(false);
    }
  }

  function handleSearch(): void {
    const values = searchForm.getFieldsValue();
    const params: ScheduleQueryParams = {};
    if (values.scheduleDate) {
      params.scheduleDate = values.scheduleDate.format('YYYY-MM-DD');
    }
    if (values.doctorId) {
      params.doctorId = values.doctorId;
    }
    if (values.deptId) {
      params.deptId = values.deptId;
    }
    void loadSchedules(params);
  }

  function handleReset(): void {
    searchForm.resetFields();
    void loadSchedules();
  }

  /** 打开新增弹窗。 */
  function handleOpenCreate(): void {
    setEditingRecord(null);
    setModalOpen(true);
  }

  /** 打开编辑弹窗。 */
  function handleOpenEdit(record: ScheduleRecord): void {
    setEditingRecord(record);
    // 联动过滤 binding 选项
    const doctorBindings = doctorDepartmentBindings.filter((b) => b.doctorId === record.doctorId);
    setFilteredBindingOptions(
      doctorBindings.map((b) => ({ label: b.label, value: `${b.doctorId}:${b.deptId}` })),
    );
    setModalOpen(true);
  }

  // 编辑弹窗打开后回填表单
  useEffect(() => {
    if (modalOpen && editingRecord) {
      const binding = doctorDepartmentBindings.find(
        (b) => b.doctorId === editingRecord.doctorId && b.deptId === editingRecord.deptId,
      );
      const defaultDate = scheduleDateOptions[0]?.value ?? editingRecord.scheduleDate;
      form.setFieldsValue({
        doctorId: editingRecord.doctorId,
        bindingKey: binding ? `${binding.doctorId}:${binding.deptId}` : `${editingRecord.doctorId}:${editingRecord.deptId}`,
        scheduleDate: editingRecord.scheduleDate || defaultDate,
        timeSlot: editingRecord.timeSlot,
        totalNumber: editingRecord.totalNumber,
      });
    }
  }, [modalOpen]);

  /** 提交新增或更新。 */
  async function handleSubmit(): Promise<void> {
    try {
      const values = await form.validateFields();
      const [doctorId, deptId] = String(values.bindingKey).split(':').map(Number);
      const payload = {
        doctorId,
        deptId,
        slot: `${values.scheduleDate} ${values.timeSlot}`,
        scheduleDate: values.scheduleDate,
        timeSlot: values.timeSlot,
        totalNumber: values.totalNumber,
      };

      setSubmitting(true);
      if (editingRecord) {
        await updateSchedule(editingRecord.id, payload);
        message.success('排班更新成功');
      } else {
        await createSchedule(payload);
        message.success('排班创建成功');
      }
      setModalOpen(false);
      void loadSchedules();
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'errorFields' in err) {
        return;
      }
      message.error(editingRecord ? '排班更新失败' : '排班创建失败');
    } finally {
      setSubmitting(false);
    }
  }

  /** 删除排班。 */
  function handleDelete(record: ScheduleRecord): void {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除${record.scheduleDate} ${record.doctorName}的排班吗？`,
      onOk: async () => {
        try {
          await deleteSchedule(record.id);
          message.success('排班已删除');
          void loadSchedules();
        } catch {
          message.error('排班删除失败');
        }
      },
    });
  }

  /** 排班日期变化时联动更新时间段。 */
  function handleScheduleDateChange(value: string): void {
    setSelectedScheduleDate(value);
    const firstSlot = scheduleTimeSlotOptions[0]?.value;
    if (firstSlot) {
      form.setFieldValue('timeSlot', firstSlot);
    }
  }

  const columns: ColumnsType<ScheduleRecord> = [
    { title: '排班编号', dataIndex: 'id', width: 90 },
    { title: '排班日期', dataIndex: 'scheduleDate', width: 110 },
    { title: '时间段', dataIndex: 'timeSlot', width: 130 },
    { title: '医生姓名', dataIndex: 'doctorName', width: 100 },
    { title: '科室名称', dataIndex: 'departmentName', width: 120 },
    { title: '总号源', dataIndex: 'totalNumber', width: 80 },
    {
      title: '剩余号源',
      dataIndex: 'remain',
      width: 90,
      render: (value: number) => (
        <Tag color={value > 0 ? 'success' : 'error'}>{value ?? 0}</Tag>
      ),
    },
    {
      title: '操作',
      key: 'actions',
      width: 160,
      render: (_, record) => (
        <Space>
          <Button type="link" onClick={() => handleOpenEdit(record)}>修改</Button>
          <Button type="link" danger onClick={() => handleDelete(record)}>删除</Button>
        </Space>
      ),
    },
  ];

  return (
    <div className="page-shell">
      <PageHero
        eyebrow="医疗资源"
        title="排班管理"
        description="按日期、医生和科室筛选查看排班列表，管理号源数据。"
        badgeText="排班管理"
      />

      {/* 筛选栏 */}
      <div className="console-card" style={{ marginBottom: 18, padding: '18px 20px' }}>
        <Form form={searchForm} layout="inline" style={{ flexWrap: 'wrap', gap: 12 }}>
          <Form.Item name="scheduleDate" label="排班日期">
            <DatePicker placeholder="选择日期" allowClear />
          </Form.Item>
          <Form.Item name="doctorId" label="医生">
            <Select
              placeholder="选择医生"
              allowClear
              showSearch
              optionFilterProp="label"
              style={{ width: 180 }}
              options={doctors.map((d) => ({ label: d.name, value: d.doctorId ?? d.id }))}
            />
          </Form.Item>
          <Form.Item name="deptId" label="科室">
            <Select
              placeholder="选择科室"
              allowClear
              showSearch
              optionFilterProp="label"
              style={{ width: 180 }}
              options={departments.map((d) => ({ label: d.name, value: d.deptId ?? d.id }))}
            />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" onClick={handleSearch}>查询</Button>
              <Button onClick={handleReset}>重置</Button>
            </Space>
          </Form.Item>
        </Form>
      </div>

      {/* 表格 */}
      <div className="console-card" style={{ padding: '18px 20px' }}>
        <div style={{ marginBottom: 16, display: 'flex', justifyContent: 'flex-end' }}>
          <Button type="primary" onClick={handleOpenCreate}>新增排班</Button>
        </div>
        <Table<ScheduleRecord>
          rowKey="id"
          columns={columns}
          dataSource={schedules}
          loading={loading}
          pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `共 ${total} 条` }}
          scroll={{ x: 'max-content' }}
        />
      </div>

      {/* 新增/编辑弹窗 */}
      <Modal
        title={editingRecord ? '修改排班' : '新增排班'}
        open={modalOpen}
        onOk={handleSubmit}
        onCancel={() => setModalOpen(false)}
        okText="保存"
        cancelText="取消"
        confirmLoading={submitting}
        width={520}
        destroyOnClose
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Form.Item name="doctorId" label="医生" rules={[{ required: true, message: '请选择医生' }]}>
            <Select
              placeholder="请选择医生"
              showSearch
              optionFilterProp="label"
              onChange={handleDoctorChange}
              options={doctors.map((d) => ({ label: d.name, value: d.doctorId ?? d.id }))}
            />
          </Form.Item>
          <Form.Item name="bindingKey" label="医生科室绑定" rules={[{ required: true, message: '请选择科室绑定关系' }]}>
            <Select
              placeholder={filteredBindingOptions.length === 0 ? '请先选择医生' : '请选择科室'}
              options={filteredBindingOptions}
              notFoundContent={filteredBindingOptions.length === 0 ? '请先选择医生' : '无匹配科室'}
            />
          </Form.Item>
          <Form.Item name="scheduleDate" label="排班日期" rules={[{ required: true, message: '请选择排班日期' }]}>
            <Select placeholder="请选择排班日期" options={scheduleDateOptions} onChange={handleScheduleDateChange} />
          </Form.Item>
          <Form.Item name="timeSlot" label="时间段" rules={[{ required: true, message: '请选择时间段' }]}>
            <Select placeholder="请选择时间段" options={scheduleTimeSlotOptions} />
          </Form.Item>
          <Form.Item name="totalNumber" label="总号源数量" rules={[{ required: true, message: '请输入总号源数量' }]}>
            <InputNumber min={1} max={999} style={{ width: '100%' }} placeholder="输入总号源数量" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
}

export default SchedulePage;