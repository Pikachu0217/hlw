import { Form, Input, InputNumber, Modal, Select, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createDepartment, fetchDepartments } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface DepartmentRecord {
  key: string;
  id: number;
  name: string;
  doctorCount: number;
  queue: string;
  status: string;
}

const columns: ColumnsType<DepartmentRecord> = [
  { title: '科室名称', dataIndex: 'name' },
  { title: '医生数量', dataIndex: 'doctorCount' },
  { title: '候诊状态', dataIndex: 'queue' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '启用' ? 'green' : 'default'}>{value}</Tag> },
];

function DepartmentsPage() {
  const { records, loading, refresh } = useModuleRecords(fetchDepartments, '科室');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const patientWaiting = records.filter((record) => record.queue.includes('等候')).length;

  const handleCreate = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await createDepartment(values);
      message.success('科室创建成功');
      setOpen(false);
      form.resetFields();
      refresh();
    } catch {
      message.warning('科室创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <ModulePage<DepartmentRecord>
        eyebrow="医生管理"
        title="科室管理"
        description="维护科室基础资料、医生数量和候诊状态，承接医生与排班关系。"
        metrics={[
          { label: '科室数', value: String(records.length), hint: '来自医生服务科室接口' },
          { label: '候诊科室', value: String(patientWaiting), hint: '按候诊描述统计' },
          { label: '医生总数', value: String(records.reduce((sum, record) => sum + record.doctorCount, 0)), hint: '汇总当前科室医生数' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="科室列表"
        searchPlaceholder="搜索科室或候诊状态"
        getSearchText={(record) => `${record.name} ${record.queue}`}
        onCreate={() => setOpen(true)}
      />
      <Modal
        title="新增科室"
        open={open}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form" initialValues={{ status: '启用', queue: '当前等候 0 人', sort: 0 }}>
          <Form.Item name="name" label="科室名称" rules={[{ required: true, message: '请输入科室名称' }]}>
            <Input placeholder="请输入科室名称" />
          </Form.Item>
          <Form.Item name="queue" label="候诊状态">
            <Input placeholder="例如：当前等候 0 人" />
          </Form.Item>
          <Form.Item name="sort" label="排序">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={[
                { label: '启用', value: '启用' },
                { label: '停用', value: '停用' },
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
