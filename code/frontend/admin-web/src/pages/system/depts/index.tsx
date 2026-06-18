import { Form, Input, InputNumber, Modal, Select, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createSystemDept, fetchSystemDepts } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';
import type { SystemDeptRecord } from '@/api/modules';

const columns: ColumnsType<SystemDeptRecord> = [
  { title: '部门名称', dataIndex: 'deptName' },
  { title: '父级编号', dataIndex: 'parentId' },
  { title: '排序', dataIndex: 'sort' },
  { title: '祖级列表', dataIndex: 'ancestors' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '启用' : '禁用'}</Tag> },
];

function SystemDeptsPage() {
  const { records, loading, refresh } = useModuleRecords(fetchSystemDepts, '系统部门');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleCreate = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await createSystemDept(values);
      message.success('部门创建成功');
      setOpen(false);
      form.resetFields();
      refresh();
    } catch {
      message.warning('部门创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <ModulePage<SystemDeptRecord>
        eyebrow="系统管理"
        title="系统部门管理"
        description="维护系统组织部门，供后台用户归属与数据权限使用。"
        metrics={[
          { label: '部门数', value: String(records.length), hint: '来自后端系统部门接口' },
          { label: '根部门', value: String(records.filter((record) => record.parentId === 0).length), hint: '按父级编号统计' },
          { label: '启用部门', value: String(records.filter((record) => record.status === '0').length), hint: '按状态实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="部门列表"
        searchPlaceholder="搜索部门名称或祖级列表"
        getSearchText={(record) => `${record.deptName} ${record.ancestors} ${record.parentId}`}
        onCreate={() => setOpen(true)}
      />
      <Modal
        title="新增部门"
        open={open}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form" initialValues={{ parentId: 0, status: '0', sort: 0 }}>
          <Form.Item name="deptName" label="部门名称" rules={[{ required: true, message: '请输入部门名称' }]}>
            <Input placeholder="请输入部门名称" />
          </Form.Item>
          <Form.Item name="parentId" label="上级部门">
            <Select
              allowClear
              placeholder="请选择上级部门"
              options={[{ label: '根部门', value: 0 }, ...records.map((dept) => ({ label: `${dept.deptName} (#${dept.id})`, value: dept.id }))]}
            />
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
        </Form>
      </Modal>
    </>
  );
}

export default SystemDeptsPage;
