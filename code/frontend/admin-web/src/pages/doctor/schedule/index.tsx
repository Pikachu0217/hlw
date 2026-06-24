import { Button, DatePicker, Form, Row, Col, Select, Space, Table, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useState } from 'react';
import {
  fetchDepartments,
  fetchDoctors,
  fetchSchedules,
  type DoctorRecord,
  type ScheduleQueryParams,
  type ScheduleRecord,
} from '@/api/modules';
import type { DepartmentRecord } from '@/pages/doctor/departments';
import PageHero from '@/components/PageHero';

function SchedulePage() {
  const [form] = Form.useForm();
  const [schedules, setSchedules] = useState<ScheduleRecord[]>([]);
  const [doctors, setDoctors] = useState<DoctorRecord[]>([]);
  const [departments, setDepartments] = useState<DepartmentRecord[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    void Promise.all([
      fetchDoctors().then(setDoctors).catch(() => {}),
      fetchDepartments().then(setDepartments).catch(() => {}),
    ]);
  }, []);

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
    const values = form.getFieldsValue();
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
    form.resetFields();
    void loadSchedules();
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
      width: 120,
      render: (_, record) => (
        <Space>
          <Button type="link" disabled>查看</Button>
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
        <Form form={form} layout="inline" style={{ flexWrap: 'wrap', gap: 12 }}>
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
        <Table<ScheduleRecord>
          rowKey="id"
          columns={columns}
          dataSource={schedules}
          loading={loading}
          pagination={{ pageSize: 20, showSizeChanger: true, showTotal: (total) => `共 ${total} 条` }}
          scroll={{ x: 'max-content' }}
        />
      </div>
    </div>
  );
}

export default SchedulePage;
