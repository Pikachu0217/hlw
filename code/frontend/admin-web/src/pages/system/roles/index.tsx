import { Button, Form, Input, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createRole, deleteRole, fetchRoles, updateRole } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface RoleRecord {
  key: string;
  roleName: string;
  roleCode: string;
  dataScope: string;
  memberCount: number;
  updatedAt: string;
  status: string;
}

function RolesPage() {
  const { records, loading, refresh } = useModuleRecords(fetchRoles, '角色');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<RoleRecord | null>(null);
  const memberCount = records.reduce((sum, record) => sum + record.memberCount, 0);

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ dataScope: '本租户数据', status: '0' });
    setOpen(true);
  };

  const handleOpenEdit = (record: RoleRecord) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateRole(editingRecord.key, values);
        message.success('角色更新成功');
      } else {
        await createRole(values);
        message.success('角色创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning(editingRecord ? '角色更新失败，请检查接口或稍后重试' : '角色创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: RoleRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除角色"${record.roleName}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteRole(record.key);
          message.success('角色删除成功');
          refresh();
        } catch {
          message.warning('角色删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<RoleRecord>>(
    () => [
      { title: '角色名称', dataIndex: 'roleName' },
      { title: '角色编码', dataIndex: 'roleCode' },
      { title: '数据范围', dataIndex: 'dataScope' },
      { title: '成员数', dataIndex: 'memberCount' },
      { title: '更新时间', dataIndex: 'updatedAt' },
      { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '启用' : '禁用'}</Tag> },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: RoleRecord) => (
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
      <ModulePage<RoleRecord>
        eyebrow="系统管理"
        title="角色与数据范围"
        description="先把角色列表、数据范围和启停状态搭清楚。"
        metrics={[
          { label: '角色数', value: String(records.length), hint: '来自后端角色接口' },
          { label: '成员数', value: String(memberCount), hint: '汇总当前角色成员' },
          { label: '启用角色', value: String(records.filter((record) => record.status === '0').length), hint: '按状态实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="角色列表"
        searchPlaceholder="搜索角色名称或数据范围"
        getSearchText={(record) => `${record.roleName} ${record.roleCode} ${record.dataScope} ${record.status}`}
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑角色' : '新增角色'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="roleName" label="角色名称" rules={[{ required: true, message: '请输入角色名称' }]}>
            <Input placeholder="请输入角色名称" />
          </Form.Item>
          <Form.Item name="roleCode" label="角色编码" rules={[{ required: true, message: '请输入角色编码' }]}>
            <Input placeholder="例如：OPERATOR" />
          </Form.Item>
          <Form.Item name="dataScope" label="数据范围">
            <Select
              options={[
                { label: '本租户数据', value: '本租户数据' },
                { label: '本科室数据', value: '本科室数据' },
                { label: '全部数据', value: '全部数据' },
              ]}
            />
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

export default RolesPage;
