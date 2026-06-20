import { Button, Form, Input, InputNumber, Modal, Select, Space, Tag, Tree, message } from 'antd';
import type { Key } from 'react';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { bindRoleMenus, createRole, deleteRole, fetchMenus, fetchRoleMenus, fetchRoles, updateRole } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';
import { buildRoleMenuTreeData } from '@/utils/menu-tree';

export interface RoleRecord {
  id: number;
  roleName: string;
  roleCode: string;
  orderNum?: number;
  dataScope: number;
  memberCount?: number;
  updatedAt?: string;
  status: number;
  isDefault?: number;
  remark?: string;
}

const dataScopeMap: Record<number, string> = {
  1: '全部数据',
  2: '自定义数据',
  3: '本部门数据',
  4: '本部门及以下数据',
  5: '仅本人数据',
};

/** 角色菜单绑定弹窗宽度。 */
const ROLE_MENU_MODAL_WIDTH = 720;

function RolesPage() {
  const { records, loading, refresh } = useModuleRecords(fetchRoles, '角色');
  const { records: menuOptions } = useModuleRecords(fetchMenus, '菜单');
  const { records: roleMenuRecords, refresh: refreshRoleMenus } = useModuleRecords(fetchRoleMenus, '角色菜单绑定');
  const [form] = Form.useForm();
  const [menuForm] = Form.useForm<{ menuIds: number[] }>();
  const [open, setOpen] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [menuSubmitting, setMenuSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<RoleRecord | null>(null);
  const [bindingRecord, setBindingRecord] = useState<RoleRecord | null>(null);
  const memberCount = records.reduce((sum, record) => sum + (record.memberCount ?? 0), 0);
  const roleMenuTreeData = useMemo(() => buildRoleMenuTreeData(menuOptions), [menuOptions]);
  const checkedMenuIds = Form.useWatch('menuIds', menuForm) ?? [];

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ dataScope: 1, orderNum: 0, status: 0 });
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
        await updateRole(editingRecord.id, values);
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
          await deleteRole(record.id);
          message.success('角色删除成功');
          refresh();
        } catch {
          message.warning('角色删除失败，请稍后重试');
        }
      },
    });
  };

  const handleOpenBindMenu = (record: RoleRecord) => {
    const menuIds = roleMenuRecords
      .filter((relation) => relation.roleId === record.id)
      .map((relation) => relation.menuId);
    setBindingRecord(record);
    menuForm.setFieldsValue({ menuIds });
    setMenuOpen(true);
  };

  const handleBindMenu = async () => {
    const values = await menuForm.validateFields();
    if (!bindingRecord) {
      return;
    }
    setMenuSubmitting(true);
    try {
      await bindRoleMenus(bindingRecord.id, values.menuIds ?? []);
      message.success('角色菜单绑定成功');
      setMenuOpen(false);
      setBindingRecord(null);
      menuForm.resetFields();
      refreshRoleMenus();
      refresh();
    } catch {
      message.warning('角色菜单绑定失败，请检查接口或稍后重试');
    } finally {
      setMenuSubmitting(false);
    }
  };

  /**
   * 同步角色菜单树勾选结果到表单。
   *
   * @param checkedKeys 勾选菜单编号
   */
  const handleMenuTreeCheck = (checkedKeys: Key[] | { checked: Key[]; halfChecked: Key[] }) => {
    const keys = Array.isArray(checkedKeys) ? checkedKeys : checkedKeys.checked;
    menuForm.setFieldsValue({ menuIds: keys.map(Number).filter(Number.isFinite) });
  };

  const columns = useMemo<ColumnsType<RoleRecord>>(
    () => [
      { title: '角色名称', dataIndex: 'roleName' },
      { title: '角色编码', dataIndex: 'roleCode' },
      { title: '排序', dataIndex: 'orderNum' },
      { title: '数据范围', dataIndex: 'dataScope', render: (value: number) => dataScopeMap[value] ?? value },
      { title: '成员数', dataIndex: 'memberCount' },
      { title: '更新时间', dataIndex: 'updatedAt' },
      {
        title: '状态',
        dataIndex: 'status',
        render: (value: number) => <Tag color={value === 0 ? 'green' : 'default'}>{value === 0 ? '启用' : '禁用'}</Tag>,
      },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: RoleRecord) => {
          const isDefaultRole = record.isDefault === 0;
          return (
            <Space size="small">
              <Button type="link" size="small" onClick={() => handleOpenEdit(record)} disabled={isDefaultRole}>
                编辑
              </Button>
              <Button type="link" size="small" onClick={() => handleOpenBindMenu(record)} disabled={isDefaultRole}>
                绑定菜单
              </Button>
              <Button type="link" size="small" danger onClick={() => handleDelete(record)} disabled={isDefaultRole}>
                删除
              </Button>
            </Space>
          );
        },
      },
    ],
    [roleMenuRecords],
  );

  return (
    <>
      <ModulePage<RoleRecord>
        eyebrow="系统管理"
        title="角色与数据范围"
        description="维护角色编码、排序、数据范围和启停状态。"
        metrics={[
          { label: '角色数', value: String(records.length), hint: '来自后端角色接口' },
          { label: '成员数', value: String(memberCount), hint: '汇总当前角色成员' },
          { label: '启用角色', value: String(records.filter((record) => record.status === 0).length), hint: '按状态实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="角色列表"
        searchPlaceholder="搜索角色名称、编码或数据范围"
        getSearchText={(record) => `${record.roleName} ${record.roleCode} ${dataScopeMap[record.dataScope] ?? ''} ${record.status}`}
        tableClassName="system-compact-table"
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
          <Form.Item name="orderNum" label="排序">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="dataScope" label="数据范围">
            <Select
              options={[
                { label: '全部数据', value: 1 },
                { label: '自定义数据', value: 2 },
                { label: '本部门数据', value: 3 },
                { label: '本部门及以下数据', value: 4 },
                { label: '仅本人数据', value: 5 },
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
      <Modal
        title={bindingRecord ? `绑定菜单：${bindingRecord.roleName}` : '绑定菜单'}
        open={menuOpen}
        width={ROLE_MENU_MODAL_WIDTH}
        confirmLoading={menuSubmitting}
        onOk={handleBindMenu}
        onCancel={() => setMenuOpen(false)}
        destroyOnClose
      >
        <Form form={menuForm} layout="vertical" className="module-form">
          <Form.Item name="menuIds" label="菜单">
            <Tree
              checkable
              defaultExpandAll
              checkedKeys={checkedMenuIds}
              treeData={roleMenuTreeData}
              onCheck={handleMenuTreeCheck}
              className="module-menu-tree"
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default RolesPage;
