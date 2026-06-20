import { Button, Form, Input, Modal, Select, Space, Tag, Tree, message } from 'antd';
import type { Key } from 'react';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createTenantPackage, deleteTenantPackage, fetchMenus, fetchTenantPackages, updateTenantPackage } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';
import { buildMenuCheckTreeData } from '@/utils/menu-tree';

export interface TenantPackageRecord {
  id: number;
  packageName: string;
  remark?: string;
  status: number;
  menuIds?: number[];
}

interface TenantPackageFormValues {
  packageName: string;
  remark?: string;
  status?: number;
  menuIds?: number[];
}

/** 租户套餐菜单绑定弹窗宽度。 */
const TENANT_PACKAGE_MENU_MODAL_WIDTH = 720;

/**
 * 将菜单树勾选结果转换为菜单编号列表。
 *
 * @param checkedKeys 勾选菜单编号
 * @return 菜单编号列表
 */
function toMenuIds(checkedKeys: Key[] | { checked: Key[]; halfChecked: Key[] }): number[] {
  const keys = Array.isArray(checkedKeys) ? checkedKeys : checkedKeys.checked;
  return keys.map(Number).filter(Number.isFinite);
}

function TenantPackagesPage() {
  const { records, loading, refresh } = useModuleRecords(fetchTenantPackages, '租户套餐');
  const { records: menuOptions } = useModuleRecords(fetchMenus, '菜单');
  const [form] = Form.useForm<TenantPackageFormValues>();
  const [menuForm] = Form.useForm<{ menuIds: number[] }>();
  const [open, setOpen] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [menuSubmitting, setMenuSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<TenantPackageRecord | null>(null);
  const [bindingRecord, setBindingRecord] = useState<TenantPackageRecord | null>(null);
  const packageMenuTreeData = useMemo(() => buildMenuCheckTreeData(menuOptions), [menuOptions]);
  const checkedFormMenuIds = Form.useWatch('menuIds', form) ?? [];
  const checkedBindMenuIds = Form.useWatch('menuIds', menuForm) ?? [];

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ status: 0 });
    setOpen(true);
  };

  const handleOpenEdit = (record: TenantPackageRecord) => {
    setEditingRecord(record);
    form.setFieldsValue({
      packageName: record.packageName,
      remark: record.remark,
      status: record.status,
      menuIds: record.menuIds ?? [],
    });
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload = {
      packageName: values.packageName,
      remark: values.remark,
      status: values.status,
      menuIds: values.menuIds ?? [],
    };
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateTenantPackage(editingRecord.id, payload);
        message.success('租户套餐更新成功');
      } else {
        await createTenantPackage(payload);
        message.success('租户套餐创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning(editingRecord ? '租户套餐更新失败，请检查接口或稍后重试' : '租户套餐创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  /**
   * 打开租户套餐菜单绑定弹窗。
   *
   * @param record 租户套餐记录
   */
  const handleOpenBindMenu = (record: TenantPackageRecord) => {
    setBindingRecord(record);
    menuForm.setFieldsValue({ menuIds: record.menuIds ?? [] });
    setMenuOpen(true);
  };

  /**
   * 保存租户套餐菜单绑定。
   */
  const handleBindMenu = async () => {
    const values = await menuForm.validateFields();
    if (!bindingRecord) {
      return;
    }
    setMenuSubmitting(true);
    try {
      await updateTenantPackage(bindingRecord.id, {
        packageName: bindingRecord.packageName,
        remark: bindingRecord.remark,
        status: bindingRecord.status,
        menuIds: values.menuIds ?? [],
      });
      message.success('租户套餐菜单绑定成功');
      setMenuOpen(false);
      setBindingRecord(null);
      menuForm.resetFields();
      refresh();
    } catch {
      message.warning('租户套餐菜单绑定失败，请检查接口或稍后重试');
    } finally {
      setMenuSubmitting(false);
    }
  };

  /**
   * 同步新增编辑表单菜单勾选结果。
   *
   * @param checkedKeys 勾选菜单编号
   */
  const handleFormMenuTreeCheck = (checkedKeys: Key[] | { checked: Key[]; halfChecked: Key[] }) => {
    form.setFieldsValue({ menuIds: toMenuIds(checkedKeys) });
  };

  /**
   * 同步快捷绑定弹窗菜单勾选结果。
   *
   * @param checkedKeys 勾选菜单编号
   */
  const handleBindMenuTreeCheck = (checkedKeys: Key[] | { checked: Key[]; halfChecked: Key[] }) => {
    menuForm.setFieldsValue({ menuIds: toMenuIds(checkedKeys) });
  };

  const handleDelete = (record: TenantPackageRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除租户套餐"${record.packageName}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteTenantPackage(record.id);
          message.success('租户套餐删除成功');
          refresh();
        } catch {
          message.warning('租户套餐删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<TenantPackageRecord>>(
    () => [
      { title: '套餐名称', dataIndex: 'packageName' },
      { title: '菜单数量', dataIndex: 'menuIds', render: (value: number[] | undefined) => value?.length ?? 0 },
      { title: '备注', dataIndex: 'remark' },
      {
        title: '状态',
        dataIndex: 'status',
        render: (value: number) => <Tag color={value === 0 ? 'green' : 'default'}>{value === 0 ? '启用' : '禁用'}</Tag>,
      },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: TenantPackageRecord) => (
          <Space size="small">
            <Button type="link" size="small" onClick={() => handleOpenEdit(record)}>
              编辑
            </Button>
            <Button type="link" size="small" onClick={() => handleOpenBindMenu(record)}>
              绑定菜单
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
      <ModulePage<TenantPackageRecord>
        eyebrow="系统管理"
        title="租户套餐管理"
        description="维护租户可用菜单集合与套餐启停状态，支撑租户授权配置。"
        metrics={[
          { label: '套餐数', value: String(records.length), hint: '来自后端租户套餐接口' },
          { label: '启用套餐', value: String(records.filter((record) => record.status === 0).length), hint: '按状态实时统计' },
          { label: '菜单授权', value: String(records.reduce((sum, record) => sum + (record.menuIds?.length ?? 0), 0)), hint: '汇总套餐菜单数量' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="租户套餐列表"
        searchPlaceholder="搜索套餐名称或备注"
        getSearchText={(record) => `${record.packageName} ${record.remark ?? ''}`}
        tableClassName="system-compact-table"
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑租户套餐' : '新增租户套餐'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="packageName" label="套餐名称" rules={[{ required: true, message: '请输入套餐名称' }]}>
            <Input placeholder="请输入套餐名称" />
          </Form.Item>
          <Form.Item name="menuIds" label="绑定菜单">
            <Tree
              checkable
              defaultExpandAll
              checkedKeys={checkedFormMenuIds}
              treeData={packageMenuTreeData}
              onCheck={handleFormMenuTreeCheck}
              className="module-menu-tree"
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
        title={bindingRecord ? `绑定菜单：${bindingRecord.packageName}` : '绑定菜单'}
        open={menuOpen}
        width={TENANT_PACKAGE_MENU_MODAL_WIDTH}
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
              checkedKeys={checkedBindMenuIds}
              treeData={packageMenuTreeData}
              onCheck={handleBindMenuTreeCheck}
              className="module-menu-tree"
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default TenantPackagesPage;
