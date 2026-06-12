import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ModulePage from '@/components/ModulePage';

interface RoleRecord {
  key: string;
  roleName: string;
  dataScope: string;
  memberCount: number;
  updatedAt: string;
  status: string;
}

const dataSource: RoleRecord[] = [
  { key: '1', roleName: '系统管理员', dataScope: '全部数据', memberCount: 3, updatedAt: '2026-06-10 11:20', status: '启用' },
  { key: '2', roleName: '运营管理员', dataScope: '本租户数据', memberCount: 11, updatedAt: '2026-06-09 17:45', status: '启用' },
];

const columns: ColumnsType<RoleRecord> = [
  { title: '角色名称', dataIndex: 'roleName' },
  { title: '数据范围', dataIndex: 'dataScope' },
  { title: '成员数', dataIndex: 'memberCount' },
  { title: '更新时间', dataIndex: 'updatedAt' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function RolesPage() {
  return (
    <ModulePage<RoleRecord>
      eyebrow="系统管理"
      title="角色与数据范围"
      description="先把角色列表、数据范围和启停状态搭清楚。"
      metrics={[
        { label: '角色数', value: '12', hint: '含平台与租户角色' },
        { label: '高权限角色', value: '3', hint: '建议纳入重点审计' },
        { label: '待调整角色', value: '2', hint: '近期存在职责变化' },
      ]}
      columns={columns}
      dataSource={dataSource}
      tableTitle="角色列表"
      searchPlaceholder="搜索角色名称或数据范围"
      getSearchText={(record) => `${record.roleName} ${record.dataScope} ${record.status}`}
    />
  );
}

export default RolesPage;
