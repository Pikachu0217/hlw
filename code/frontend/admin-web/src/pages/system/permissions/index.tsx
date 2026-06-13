import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchPermissions } from '@/api/modules';
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
  const { records, loading } = useModuleRecords(fetchPermissions, '权限码');

  return (
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
    />
  );
}

export default PermissionsPage;
