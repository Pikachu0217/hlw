import { Button, Form, Input, InputNumber, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createSystemDept, deleteSystemDept, fetchSystemDepts, updateSystemDept } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';
import type { SystemDeptRecord } from '@/api/modules';

function SystemDeptsPage() {
  const { records, loading, refresh } = useModuleRecords(fetchSystemDepts, '系统部门');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<SystemDeptRecord | null>(null);

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ parentId: 0, status: 0, orderNum: 0 });
    setOpen(true);
  };

  const handleOpenEdit = (record: SystemDeptRecord) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateSystemDept(editingRecord.id, values);
        message.success('部门更新成功');
      } else {
        await createSystemDept(values);
        message.success('部门创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning(editingRecord ? '部门更新失败，请检查接口或稍后重试' : '部门创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: SystemDeptRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除部门"${record.deptName}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteSystemDept(record.id);
          message.success('部门删除成功');
          refresh();
        } catch {
          message.warning('部门删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<SystemDeptRecord>>(
    () => [
      { title: '部门名称', dataIndex: 'deptName' },
      { title: '父级编号', dataIndex: 'parentId' },
      { title: '排序', dataIndex: 'orderNum' },
      { title: '负责人', dataIndex: 'leader' },
      { title: '电话', dataIndex: 'phone' },
      { title: '祖级列表', dataIndex: 'ancestors' },
      {
        title: '状态',
        dataIndex: 'status',
        render: (value: number) => <Tag color={value === 0 ? 'green' : 'default'}>{value === 0 ? '启用' : '禁用'}</Tag>,
      },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: SystemDeptRecord) => (
          <Space size="small">
            <Button type="link" size="small" onClick={() => handleOpenEdit(record)}>
              编辑
            </Button>
            <Button type="link" size="small" danger onClick={() => handleDelete(record)}>
              删除
            </Button>
          </Space>
        ),
      },
    ],
    [],
  );

  return (
    <>
      <ModulePage<SystemDeptRecord>
        eyebrow="系统管理"
        title="系统部门管理"
        description="维护系统组织部门，供后台用户归属与数据权限使用。"
        metrics={[
          { label: '部门数', value: String(records.length), hint: '来自后端系统部门接口' },
          { label: '根部门', value: String(records.filter((record) => record.parentId === 0).length), hint: '按父级编号统计' },
          { label: '启用部门', value: String(records.filter((record) => record.status === 0).length), hint: '按状态实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="部门列表"
        searchPlaceholder="搜索部门名称、负责人或祖级列表"
        getSearchText={(record) => `${record.deptName} ${record.leader ?? ''} ${record.ancestors} ${record.parentId}`}
        tableClassName="system-compact-table"
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑部门' : '新增部门'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
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
          <Form.Item name="orderNum" label="排序">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="leader" label="负责人">
            <Input placeholder="请输入负责人" />
          </Form.Item>
          <Form.Item name="phone" label="联系电话">
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input placeholder="请输入邮箱" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={[
                { label: '启用', value: 0 },
                { label: '禁用', value: 1 },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default SystemDeptsPage;
