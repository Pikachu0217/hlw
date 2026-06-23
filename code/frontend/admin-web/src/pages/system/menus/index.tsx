import {
  BookOutlined,
  CalendarOutlined,
  CloudServerOutlined,
  DashboardOutlined,
  DeploymentUnitOutlined,
  EditOutlined,
  ExperimentOutlined,
  FileTextOutlined,
  IdcardOutlined,
  MedicineBoxOutlined,
  NotificationOutlined,
  PartitionOutlined,
  SafetyCertificateOutlined,
  SettingOutlined,
  ShopOutlined,
  SolutionOutlined,
  TeamOutlined,
  UserOutlined,
} from '@ant-design/icons';
import { Button, Form, Input, InputNumber, Modal, Select, Space, Tag, TreeSelect, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import type { ReactNode } from 'react';
import { useMemo, useState } from 'react';
import { createMenu, deleteMenu, fetchMenus, updateMenu } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';
import { ADMIN_NAVIGATION_REFRESH_EVENT } from '@/router/navigation';
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
  isDefault?: number;
  remark?: string;
  children?: MenuRecord[];
}

const menuTypeMap: Record<string, string> = {
  M: '目录',
  C: '菜单',
  F: '按钮',
};

interface MenuIconOption {
  /** 图标名称。 */
  label: string;
  /** 图标键值。 */
  value: string;
  /** 图标节点。 */
  icon: ReactNode;
}

const menuIconOptions: MenuIconOption[] = [
  { label: '工作台', value: 'dashboard', icon: <DashboardOutlined /> },
  { label: '租户', value: 'tenant', icon: <ShopOutlined /> },
  { label: '系统', value: 'system', icon: <SafetyCertificateOutlined /> },
  { label: '用户', value: 'user', icon: <TeamOutlined /> },
  { label: '用户组', value: 'peoples', icon: <TeamOutlined /> },
  { label: '角色', value: 'role', icon: <SafetyCertificateOutlined /> },
  { label: '菜单', value: 'menu', icon: <SafetyCertificateOutlined /> },
  { label: '字典', value: 'dict', icon: <BookOutlined /> },
  { label: '文档', value: 'documentation', icon: <BookOutlined /> },
  { label: '参数', value: 'tree-table', icon: <SettingOutlined /> },
  { label: '编辑', value: 'edit', icon: <EditOutlined /> },
  { label: '岗位', value: 'post', icon: <IdcardOutlined /> },
  { label: '通知', value: 'notice', icon: <NotificationOutlined /> },
  { label: '日志', value: 'log', icon: <FileTextOutlined /> },
  { label: '部门', value: 'dept', icon: <PartitionOutlined /> },
  { label: '网关', value: 'gateway', icon: <CloudServerOutlined /> },
  { label: '医生', value: 'doctor', icon: <MedicineBoxOutlined /> },
  { label: '患者', value: 'patient', icon: <TeamOutlined /> },
  { label: '咨询', value: 'consult', icon: <SolutionOutlined /> },
  { label: '预约', value: 'appointment', icon: <CalendarOutlined /> },
  { label: '处方', value: 'prescription', icon: <ExperimentOutlined /> },
  { label: '药品', value: 'drug', icon: <DeploymentUnitOutlined /> },
  { label: '订单', value: 'order', icon: <PartitionOutlined /> },
  { label: '文件', value: 'file', icon: <FileTextOutlined /> },
  { label: '个人', value: 'user2', icon: <UserOutlined /> },
];

const menuIconSelectOptions = menuIconOptions.map((item) => ({
  label: item.label,
  value: item.value,
  icon: item.icon,
}));

/** 通知后台布局重新拉取后端菜单路由树。 */
function emitNavigationRefresh(): void {
  window.dispatchEvent(new Event(ADMIN_NAVIGATION_REFRESH_EVENT));
}

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
    form.resetFields();
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
      emitNavigationRefresh();
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
          emitNavigationRefresh();
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
        width: 150,
        className: 'menu-config-table__name-column',
        render: (value: string) => <span className="menu-config-table__name">{value}</span>,
      },
      { title: '图标', dataIndex: 'icon', width: 48, render: (value: string) => resolveMenuIcon(value) },
      { title: '类型', dataIndex: 'menuType', width: 56, render: (value: string) => menuTypeMap[value] ?? value },
      { title: '权限标识', dataIndex: 'perms', width: 132 },
      { title: '路由路径', dataIndex: 'path', width: 110 },
      { title: '组件路径', dataIndex: 'component', width: 128 },
      { title: '排序', dataIndex: 'orderNum', width: 48 },
      {
        title: '状态',
        dataIndex: 'status',
        width: 62,
        render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '启用' : '禁用'}</Tag>,
      },
      {
        title: '操作',
        key: 'actions',
        width: 86,
        className: 'menu-config-table__actions-column',
        render: (_: unknown, record: MenuRecord) => {
          const isDefaultMenu = record.isDefault === 0;
          return (
            <Space size="small">
              <Button type="link" size="small" onClick={() => handleOpenEdit(record)} disabled={isDefaultMenu}>
                编辑
              </Button>
              <Button type="link" size="small" danger onClick={() => handleDelete(record)} disabled={isDefaultMenu}>
                删除
              </Button>
            </Space>
          );
        },
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
        tableClassName="system-compact-table menu-config-table"
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
            <Select
              allowClear
              showSearch
              optionFilterProp="label"
              placeholder="请选择菜单图标"
              options={menuIconSelectOptions}
              optionRender={(option) => renderMenuIconOption(option.data as MenuIconOption)}
            />
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

/**
 * 渲染菜单图标下拉选项。
 *
 * @param option 菜单图标选项
 * @return 图标选项节点
 */
function renderMenuIconOption(option: MenuIconOption): ReactNode {
  return (
    <span className="menu-icon-option">
      {option.icon}
      <span>{option.label}</span>
      <span className="menu-icon-option__value">{option.value}</span>
    </span>
  );
}

/**
 * 解析菜单图标节点。
 *
 * @param value 图标键值
 * @return 图标节点
 */
function resolveMenuIcon(value?: string): ReactNode {
  const iconOption = menuIconOptions.find((item) => item.value === value);
  return iconOption ? <span className="menu-config-table__icon">{iconOption.icon}</span> : '-';
}

export default MenusPage;
