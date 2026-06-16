import { Form, Input, InputNumber, Modal, Select, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createMenu, fetchMenus } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface MenuRecord {
  key: string;
  menuName: string;
  menuType: string;
  permission: string;
  routePath: string;
  status: string;
}

const columns: ColumnsType<MenuRecord> = [
  { title: '菜单名称', dataIndex: 'menuName' },
  { title: '类型', dataIndex: 'menuType' },
  { title: '权限标识', dataIndex: 'permission' },
  { title: '路由路径', dataIndex: 'routePath' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '启用' : '禁用'}</Tag> },
];

function MenusPage() {
  const { records, loading, refresh } = useModuleRecords(fetchMenus, '菜单');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleCreate = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await createMenu(values);
      message.success('菜单创建成功');
      setOpen(false);
      form.resetFields();
      refresh();
    } catch {
      message.warning('菜单创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <ModulePage<MenuRecord>
        eyebrow="系统管理"
        title="菜单与权限标识"
        description="把路由、权限标识与按钮位关系先搭好。"
        metrics={[
          { label: '菜单节点', value: String(records.length), hint: '来自后端菜单接口' },
          { label: '启用菜单', value: String(records.filter((record) => record.status === '0').length), hint: '按状态实时统计' },
          { label: '权限标识', value: String(records.filter((record) => record.permission).length), hint: '覆盖当前返回菜单' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="菜单配置"
        searchPlaceholder="搜索菜单、权限标识或路由"
        getSearchText={(record) => `${record.menuName} ${record.permission} ${record.routePath}`}
        onCreate={() => setOpen(true)}
      />
      <Modal
        title="新增菜单"
        open={open}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form" initialValues={{ menuType: '菜单', parentId: 0, sort: 0, status: '0' }}>
          <Form.Item name="menuName" label="菜单名称" rules={[{ required: true, message: '请输入菜单名称' }]}>
            <Input placeholder="请输入菜单名称" />
          </Form.Item>
          <Form.Item name="permission" label="权限标识" rules={[{ required: true, message: '请输入权限标识' }]}>
            <Input placeholder="例如：system:user:list" />
          </Form.Item>
          <Form.Item name="routePath" label="路由路径" rules={[{ required: true, message: '请输入路由路径' }]}>
            <Input placeholder="例如：/system/users" />
          </Form.Item>
          <Form.Item name="menuType" label="菜单类型">
            <Select
              options={[
                { label: '目录', value: '目录' },
                { label: '菜单', value: '菜单' },
                { label: '按钮', value: '按钮' },
              ]}
            />
          </Form.Item>
          <Form.Item name="parentId" label="父级编号">
            <InputNumber min={0} className="module-form__number" />
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

export default MenusPage;
