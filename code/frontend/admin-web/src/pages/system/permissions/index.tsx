import { Form, Input, InputNumber, Modal, Select, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createPermission, fetchPermissions } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface PermissionRecord {
  key: string;
  permissionName: string;
  permissionCode: string;
  resourceType: string;
  menuName: string;
  status: string;
}

const columns: ColumnsType<PermissionRecord> = [
  { title: '权限名称', dataIndex: 'permissionName' },
  { title: '权限编码', dataIndex: 'permissionCode' },
  { title: '资源类型', dataIndex: 'resourceType' },
  { title: '关联菜单', dataIndex: 'menuName' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function PermissionsPage() {
  const { records, loading, refresh } = useModuleRecords(fetchPermissions, '权限码');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleCreate = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await createPermission(values);
      message.success('权限码创建成功');
      setOpen(false);
      form.resetFields();
      refresh();
    } catch {
      message.warning('权限码创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <ModulePage<PermissionRecord>
        eyebrow="系统管理"
        title="权限管理"
        description="按菜单和按钮沉淀权限码，为角色菜单授权和后续按钮级控制做准备。"
        metrics={[
          { label: '权限码', value: String(records.length), hint: '来自后端权限接口' },
          { label: '菜单权限', value: String(records.filter((record) => record.resourceType === '菜单').length), hint: '页面访问权限' },
          { label: '按钮权限', value: String(records.filter((record) => record.resourceType === '按钮').length), hint: '操作级权限' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="权限码列表"
        searchPlaceholder="搜索权限名称、编码或菜单"
        getSearchText={(record) => `${record.permissionName} ${record.permissionCode} ${record.menuName}`}
        onCreate={() => setOpen(true)}
      />
      <Modal
        title="新增权限码"
        open={open}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form" initialValues={{ resourceType: '按钮', status: '启用' }}>
          <Form.Item name="permissionName" label="权限名称" rules={[{ required: true, message: '请输入权限名称' }]}>
            <Input placeholder="请输入权限名称" />
          </Form.Item>
          <Form.Item name="permissionCode" label="权限编码" rules={[{ required: true, message: '请输入权限编码' }]}>
            <Input placeholder="例如：system:user:create" />
          </Form.Item>
          <Form.Item name="resourceType" label="资源类型">
            <Select
              options={[
                { label: '菜单', value: '菜单' },
                { label: '按钮', value: '按钮' },
                { label: '接口', value: '接口' },
              ]}
            />
          </Form.Item>
          <Form.Item name="menuId" label="关联菜单编号">
            <InputNumber min={1} className="module-form__number" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={[
                { label: '启用', value: '启用' },
                { label: '停用', value: '停用' },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default PermissionsPage;
