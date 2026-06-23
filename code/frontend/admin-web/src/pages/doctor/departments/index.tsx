import { Button, Form, Input, InputNumber, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { fetchDepartments, updateDepartmentExtension } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface DepartmentRecord {
  id: number;
  deptId?: number;
  departmentId?: number;
  name: string;
  doctorCount: number;
  queue: string;
  status: string;
  configured?: boolean;
}

function DepartmentsPage() {
  const { records, loading, refresh } = useModuleRecords(fetchDepartments, '科室');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<DepartmentRecord | null>(null);
  const patientWaiting = records.filter((record) => record.queue.includes('等候')).length;

  const handleOpenEdit = (record: DepartmentRecord) => {
    setEditingRecord(record);
    form.setFieldsValue({
      deptId: record.deptId ?? record.id,
      name: record.name,
      queue: record.queue,
      sort: 0,
      status: record.status,
    });
    setOpen(true);
  };

  const handleSaveExtension = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await updateDepartmentExtension(values.deptId, values);
      message.success('科室扩展属性已更新');
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning('科室扩展属性更新失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const columns = useMemo<ColumnsType<DepartmentRecord>>(
    () => [
      { title: '科室名称', dataIndex: 'name' },
      { title: '医生数量', dataIndex: 'doctorCount' },
      { title: '候诊状态', dataIndex: 'queue' },
      { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '0' || value === '启用' ? 'green' : 'default'}>{value === '0' || value === '启用' ? '启用' : '禁用'}</Tag> },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: DepartmentRecord) => (
          <Space size="small">
            <Button type="link" size="small" onClick={() => handleOpenEdit(record)}>
              编辑
            </Button>
          </Space>
        ),
      },
    ],
    [],
  );

  return (
    <>
      <ModulePage<DepartmentRecord>
        eyebrow="医疗资源"
        title="科室资源"
        description="将已有基础科室开放为线上科室资源，并维护展示名称、排序和候诊状态。"
        metrics={[
          { label: '科室数', value: String(records.length), hint: '来自医生服务科室接口' },
          { label: '候诊科室', value: String(patientWaiting), hint: '按候诊描述统计' },
          { label: '医生总数', value: String(records.reduce((sum, record) => sum + record.doctorCount, 0)), hint: '汇总当前科室医生数' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="科室资源列表"
        searchPlaceholder="搜索科室或候诊状态"
        getSearchText={(record) => `${record.name} ${record.queue}`}
      />
      <Modal
        title={editingRecord ? `编辑科室扩展属性：${editingRecord.name}` : '编辑科室扩展属性'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSaveExtension}
        onCancel={() => {
          setOpen(false);
          setEditingRecord(null);
        }}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form" initialValues={{ status: '0', queue: '当前等候 0 人', sort: 0 }}>
          <Form.Item name="deptId" label="系统科室编号" rules={[{ required: true, message: '请输入系统科室编号' }]}>
            <InputNumber min={1} disabled className="module-form__number" />
          </Form.Item>
          <Form.Item name="name" label="线上展示名称" rules={[{ required: true, message: '请输入线上展示名称' }]}>
            <Input placeholder="请输入科室名称" />
          </Form.Item>
          <Form.Item name="queue" label="候诊展示">
            <Input placeholder="例如：当前等候 0 人" />
          </Form.Item>
          <Form.Item name="sort" label="排序">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={[
                { label: '启用', value: '0' },
                { label: '禁用', value: '1' },
              ]}
            />
          </Form.Item>
          <Form.Item name="description" label="科室说明">
            <Input.TextArea rows={3} placeholder="请输入科室说明" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default DepartmentsPage;
