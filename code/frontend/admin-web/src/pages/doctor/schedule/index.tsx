import { Button, DatePicker, Form, Row, Col, Select, Space, Table, Tag, message, Modal, InputNumber, Input } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useState } from 'react';
import {
  fetchDepartments,
  fetchDoctors,
  fetchSchedules,
  createSchedule,
  updateSchedule,
  deleteSchedule,
  type DoctorRecord,
  type ScheduleQueryParams,
  type ScheduleRecord,
} from '@/api/modules';
import type { DepartmentRecord } from '@/pages/doctor/departments';
import PageHero from '@/components/PageHero';

const TIME_SLOT_OPTIONS = [
  { label: '上午', value: '上午' },
  { label: '下午', value: '下午' },
  { label: '夜间', value: '夜间' },
];

function SchedulePage() {
  const [form] = Form.useForm();
  const [searchForm] = Form.useForm();
  const [schedules, setSchedules] = useState<ScheduleRecord[]>([]);
  const [doctors, setDoctors] = useState<DoctorRecord[]>([]);
  const [departments, setDepartments] = useState<DepartmentRecord[]>([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<ScheduleRecord | null>(null);
  const [filteredDeptOptions, setFilteredDeptOptions] = useState<{ label: string; value: number }[]>([]);

  useEffect(() => {
    void Promise.all([
      fetchDoctors().then(setDoctors).catch(() => {}),
      fetchDepartments().then(setDepartments).catch(() => {}),
    ]);
    void loadSchedules();
  }, []);

  useEffect(() => {
    if (!modalOpen) {
      setEditingRecord(null);
      form.resetFields();
      setFilteredDeptOptions([]);
    }
  }, [modalOpen]);

  /** 选中医生时联动过滤其关联科室。 */
  function handleDoctorChange(doctorId: number): void {
    const doctor = doctors.find((d) => (d.doctorId ?? d.id) === doctorId);
    const deptIds = doctor?.deptIds;
    if (deptIds && deptIds.length > 0) {
      // 按医生关联的科室编号过滤
      const matched = departments.filter((d) => deptIds.includes(d.deptId ?? d.id));
      setFilteredDeptOptions(matched.map((d) => ({ label: d.name, value: d.deptId ?? d.id })));
      // 如果当前选中的科室不在过滤结果中，清空
      const currentDeptId = form.getFieldValue('deptId');
      if (currentDeptId && !matched.some((d) => (d.deptId ?? d.id) === currentDeptId)) {
        form.setFieldValue('deptId', undefined);
      }
    } else {
      // 医生无科室绑定信息时展示全部科室
      setFilteredDeptOptions(departments.map((d) => ({ label: d.name, value: d.deptId ?? d.id })));
    }
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
    form.setFieldsValue({
      doctorId: record.doctorId,
      deptId: record.deptId,
      slot: record.slot,
      scheduleDate: record.scheduleDate,
      timeSlot: record.timeSlot,
      totalNumber: record.totalNumber,
    });
    // 编辑时联动过滤科室
    const doctor = doctors.find((d) => (d.doctorId ?? d.id) === record.doctorId);
    const deptIds = doctor?.deptIds;
    if (deptIds && deptIds.length > 0) {
      const matched = departments.filter((d) => deptIds.includes(d.deptId ?? d.id));
      setFilteredDeptOptions(matched.map((d) => ({ label: d.name, value: d.deptId ?? d.id })));
    } else {
      setFilteredDeptOptions(departments.map((d) => ({ label: d.name, value: d.deptId ?? d.id })));
    }
    setModalOpen(true);
  }

  /** 提交新增或更新。 */
  async function handleSubmit(): Promise<void> {
    try {
      const values = await form.validateFields();
      if (editingRecord) {
        await updateSchedule(editingRecord.id, values);
        message.success('排班更新成功');
      } else {
        await createSchedule(values);
        message.success('排班创建成功');
      }
      setModalOpen(false);
      void loadSchedules();
    } catch (err: unknown) {
      if (err && typeof err === 'object' && 'errorFields' in err) {
        return; // 表单校验未通过
      }
      message.error(editingRecord ? '排班更新失败' : '排班创建失败');
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
        width={560}
      >
        <Form form={form} layout="vertical" style={{ marginTop: 16 }}>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="doctorId" label="医生" rules={[{ required: true, message: '请选择医生' }]}>
                <Select
                  placeholder="选择医生"
                  showSearch
                  optionFilterProp="label"
                  onChange={handleDoctorChange}
                  options={doctors.map((d) => ({ label: d.name, value: d.doctorId ?? d.id }))}
                />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="deptId" label="科室" rules={[{ required: true, message: '请选择科室' }]}>
                <Select
                  placeholder="选择科室"
                  showSearch
                  optionFilterProp="label"
                  options={filteredDeptOptions}
                  notFoundContent={filteredDeptOptions.length === 0 ? '请先选择医生' : '无匹配科室'}
                />
              </Form.Item>
            </Col>
          </Row>
          <Row gutter={16}>
            <Col span={12}>
              <Form.Item name="scheduleDate" label="排班日期">
                <DatePicker style={{ width: '100%' }} placeholder="选择日期" />
              </Form.Item>
            </Col>
            <Col span={12}>
              <Form.Item name="timeSlot" label="时间段" rules={[{ required: true, message: '请选择时间段' }]}>
                <Select placeholder="选择时间段" options={TIME_SLOT_OPTIONS} />
              </Form.Item>
            </Col>
          </Row>
          <Form.Item name="slot" label="排班描述" rules={[{ required: true, message: '请输入排班描述' }]}>
            <Input placeholder="例如：2026-06-25 上午" />
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
