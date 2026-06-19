import { Button, Form, Input, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createUser, deleteUser, fetchSystemDeptOptions, fetchUsers, updateUser } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface UserRecord {
  key: string;
  userId: string;
  userName: string;
  nickName?: string;
  deptId?: number;
  deptName?: string;
  roleName?: string;
  postName?: string;
  phone?: string;
  email?: string;
  userType?: string;
  lastLogin?: string;
  status: number;
  remark?: string;
}

function UsersPage() {
  const { records, loading, refresh } = useModuleRecords(fetchUsers, '用户');
  const { records: deptOptions } = useModuleRecords(fetchSystemDeptOptions, '系统部门');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<UserRecord | null>(null);

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ userType: 'ADMIN', status: 0 });
    setOpen(true);
  };

  const handleOpenEdit = (record: UserRecord) => {
    setEditingRecord(record);
    form.setFieldsValue({
      userName: record.userName,
      nickName: record.nickName,
      phone: record.phone,
      email: record.email,
      deptId: record.deptId,
      userType: record.userType ?? 'ADMIN',
      status: record.status,
      remark: record.remark,
    });
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateUser(editingRecord.key, values);
        message.success('用户更新成功');
      } else {
        await createUser(values);
        message.success('用户创建成功');
      }
      setOpen(false);
      form.resetFields();
      setEditingRecord(null);
      refresh();
    } catch {
      message.warning(editingRecord ? '用户更新失败，请检查接口或稍后重试' : '用户创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: UserRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除用户"${record.userName}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteUser(record.key);
          message.success('用户删除成功');
          refresh();
        } catch {
          message.warning('用户删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<UserRecord>>(
    () => [
      { title: '账号', dataIndex: 'userName' },
      { title: '昵称', dataIndex: 'nickName' },
      { title: '部门', dataIndex: 'deptName' },
      { title: '角色', dataIndex: 'roleName' },
      { title: '联系电话', dataIndex: 'phone' },
      { title: '邮箱', dataIndex: 'email' },
      { title: '最近登录', dataIndex: 'lastLogin' },
      {
        title: '状态',
        dataIndex: 'status',
        render: (value: number) => <Tag color={value === 0 ? 'green' : 'default'}>{value === 0 ? '启用' : '禁用'}</Tag>,
      },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: UserRecord) => (
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
      <ModulePage<UserRecord>
        eyebrow="系统管理"
        title="后台用户清单"
        description="沉淀账号、昵称、部门、角色和登录信息。"
        metrics={[
          { label: '启用账号', value: String(records.filter((record) => record.status === 0).length), hint: '来自后端用户接口' },
          { label: '用户总数', value: String(records.length), hint: '覆盖当前后台账号' },
          { label: '业务组', value: String(new Set(records.map((record) => record.deptName).filter(Boolean)).size), hint: '按部门字段实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="用户列表"
        searchPlaceholder="搜索账号、昵称、部门、角色"
        getSearchText={(record) => `${record.userName} ${record.nickName ?? ''} ${record.deptName ?? ''} ${record.roleName ?? ''} ${record.phone ?? ''}`}
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑用户' : '新增用户'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="userName" label="账号" rules={[{ required: true, message: '请输入账号' }]}>
            <Input placeholder="请输入账号" />
          </Form.Item>
          <Form.Item name="nickName" label="昵称">
            <Input placeholder="请输入昵称" />
          </Form.Item>
          <Form.Item name="phone" label="联系电话">
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input placeholder="请输入邮箱" />
          </Form.Item>
          <Form.Item name="password" label="登录密码">
            <Input.Password placeholder={editingRecord ? '留空则不修改密码' : '默认 123456'} />
          </Form.Item>
          <Form.Item name="deptId" label="部门">
            <Select
              allowClear
              showSearch
              optionFilterProp="label"
              placeholder="请选择部门"
              options={deptOptions.map((dept) => ({ label: dept.deptName, value: dept.id }))}
            />
          </Form.Item>
          <Form.Item name="userType" label="用户类型">
            <Select
              options={[
                { label: '管理员', value: 'ADMIN' },
                { label: '医生', value: 'DOCTOR' },
                { label: '药师', value: 'PHARMACIST' },
              ]}
            />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={[
                { label: '启用', value: 0 },
                { label: '禁用', value: 1 },
              ]}
            />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default UsersPage;
