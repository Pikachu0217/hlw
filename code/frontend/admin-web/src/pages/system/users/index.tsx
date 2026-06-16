import { Form, Input, Modal, Select, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createUser, fetchUsers } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface UserRecord {
  key: string;
  username: string;
  deptName: string;
  roleName: string;
  phone: string;
  lastLogin: string;
  status: string;
}

const columns: ColumnsType<UserRecord> = [
  { title: '账号名称', dataIndex: 'username' },
  { title: '部门', dataIndex: 'deptName' },
  { title: '角色', dataIndex: 'roleName' },
  { title: '联系电话', dataIndex: 'phone' },
  { title: '最近登录', dataIndex: 'lastLogin' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '启用' : '禁用'}</Tag> },
];

function UsersPage() {
  const { records, loading, refresh } = useModuleRecords(fetchUsers, '用户');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleCreate = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await createUser(values);
      message.success('用户创建成功');
      setOpen(false);
      form.resetFields();
      refresh();
    } catch {
      message.warning('用户创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <ModulePage<UserRecord>
        eyebrow="系统管理"
        title="后台用户清单"
        description="沉淀账号、角色、部门和登录信息。"
        metrics={[
          { label: '启用账号', value: String(records.filter((record) => record.status === '0').length), hint: '来自后端用户接口' },
          { label: '用户总数', value: String(records.length), hint: '覆盖当前后台账号' },
          { label: '业务组', value: String(new Set(records.map((record) => record.deptName)).size), hint: '按部门字段实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="用户列表"
        searchPlaceholder="搜索账号、部门、角色"
        getSearchText={(record) => `${record.username} ${record.deptName} ${record.roleName} ${record.phone}`}
        onCreate={() => setOpen(true)}
      />
      <Modal
        title="新增用户"
        open={open}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form" initialValues={{ userType: 'ADMIN', status: '0', deptName: '运营部', roleName: '系统管理员' }}>
          <Form.Item name="username" label="账号名称" rules={[{ required: true, message: '请输入账号名称' }]}>
            <Input placeholder="请输入账号名称" />
          </Form.Item>
          <Form.Item name="phone" label="联系电话">
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item name="deptName" label="部门">
            <Input placeholder="请输入部门" />
          </Form.Item>
          <Form.Item name="roleName" label="角色">
            <Input placeholder="请输入角色" />
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

export default UsersPage;
