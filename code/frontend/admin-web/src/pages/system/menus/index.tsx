import { Button, Form, Input, InputNumber, Modal, Select, Space, Tag, TreeSelect, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createMenu, deleteMenu, fetchMenus, updateMenu } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';
import {
  buildMenuTree,
  buildParentMenuTreeData,
  flattenMenuTree,
  filterMenuTree,
  type MenuTreeRecord,
} from '@/utils/menu-tree';

export interface MenuRecord {
  id: number;
  parentId?: number;
  menuName: string;
  menuType: string;
  perms?: string;
  path?: string;
  component?: string;
  isFrame?: number;
  visible?: string;
  orderNum?: number;
  icon?: string;
  status: string;
  remark?: string;
  children?: MenuRecord[];
}

const menuTypeMap: Record<string, string> = {
  M: '目录',
  C: '菜单',
  F: '按钮',
};
const MENU_TABLE_SCROLL_WIDTH = 1380;

function MenusPage() {
  const { records, loading, refresh } = useModuleRecords(fetchMenus, '菜单');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<MenuRecord | null>(null);
  const allMenuRecords = useMemo(() => flattenMenuTree(records), [records]);
  const menuTreeData = useMemo(() => buildMenuTree(records), [records]);
  const parentMenuTreeData = useMemo(
    () => buildParentMenuTreeData(records, editingRecord?.id),
    [editingRecord?.id, records],
  );

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ menuType: 'C', parentId: 0, orderNum: 0, isFrame: 1, visible: '0', status: '0' });
    setOpen(true);
  };

  const handleOpenEdit = (record: MenuRecord) => {
    setEditingRecord(record);
    form.setFieldsValue({ ...record, parentId: Number(record.parentId ?? 0) });
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateMenu(editingRecord.id, values);
        message.success('菜单更新成功');
      } else {
        await createMenu(values);
        message.success('菜单创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning(editingRecord ? '菜单更新失败，请检查接口或稍后重试' : '菜单创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: MenuRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除菜单"${record.menuName}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteMenu(record.id);
          message.success('菜单删除成功');
          refresh();
        } catch {
          message.warning('菜单删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<MenuTreeRecord<MenuRecord>>>(
    () => [
      {
        title: '菜单名称',
        dataIndex: 'menuName',
        width: 280,
        className: 'menu-config-table__name-column',
        render: (value: string) => <span className="menu-config-table__name">{value}</span>,
      },
      { title: '类型', dataIndex: 'menuType', width: 96, render: (value: string) => menuTypeMap[value] ?? value },
      { title: '权限标识', dataIndex: 'perms', width: 280 },
      { title: '路由路径', dataIndex: 'path', width: 240 },
      { title: '组件路径', dataIndex: 'component', width: 280 },
      { title: '排序', dataIndex: 'orderNum', width: 90 },
      {
        title: '状态',
        dataIndex: 'status',
        width: 110,
        render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '启用' : '禁用'}</Tag>,
      },
      {
        title: '操作',
        key: 'actions',
        width: 100,
        fixed: 'right',
        render: (_: unknown, record: MenuRecord) => (
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
      <ModulePage<MenuTreeRecord<MenuRecord>>
        eyebrow="系统管理"
        title="菜单与按钮权限"
        description="维护路由节点、组件路径与按钮权限标识，权限控制统一读取菜单 perms。"
        metrics={[
          { label: '菜单节点', value: String(allMenuRecords.length), hint: '来自后端菜单接口' },
          { label: '启用菜单', value: String(allMenuRecords.filter((record) => record.status === '0').length), hint: '按状态实时统计' },
          { label: '按钮权限', value: String(allMenuRecords.filter((record) => record.menuType === 'F' || record.perms).length), hint: '覆盖当前返回菜单' },
        ]}
        columns={columns}
        dataSource={menuTreeData}
        loading={loading}
        tableTitle="菜单配置"
        searchPlaceholder="搜索菜单、权限标识或路由"
        getSearchText={(record) => `${record.menuName} ${record.perms ?? ''} ${record.path ?? ''} ${record.component ?? ''}`}
        filterDataSource={filterMenuTree}
        tableClassName="menu-config-table"
        tableScrollX={MENU_TABLE_SCROLL_WIDTH}
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑菜单' : '新增菜单'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="menuName" label="菜单名称" rules={[{ required: true, message: '请输入菜单名称' }]}>
            <Input placeholder="请输入菜单名称" />
          </Form.Item>
          <Form.Item name="perms" label="权限标识">
            <Input placeholder="例如：system:user:list" />
          </Form.Item>
          <Form.Item name="path" label="路由路径">
            <Input placeholder="例如：system/user" />
          </Form.Item>
          <Form.Item name="component" label="组件路径">
            <Input placeholder="例如：system/user/index" />
          </Form.Item>
          <Form.Item name="menuType" label="菜单类型">
            <Select
              options={[
                { label: '目录', value: 'M' },
                { label: '菜单', value: 'C' },
                { label: '按钮', value: 'F' },
              ]}
            />
          </Form.Item>
          <Form.Item name="parentId" label="父级菜单">
            <TreeSelect
              allowClear
              treeDefaultExpandAll
              treeData={parentMenuTreeData}
              placeholder="请选择父级菜单"
              className="module-form__tree-select"
            />
          </Form.Item>
          <Form.Item name="orderNum" label="排序">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="icon" label="图标">
            <Input placeholder="例如：user" />
          </Form.Item>
          <Form.Item name="visible" label="显示状态">
            <Select
              options={[
                { label: '显示', value: '0' },
                { label: '隐藏', value: '1' },
              ]}
            />
          </Form.Item>
          <Form.Item name="isFrame" label="外链">
            <Select
              options={[
                { label: '否', value: 1 },
                { label: '是', value: 0 },
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

export default MenusPage;
