import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchRoles } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface RoleRecord {
  key: string;
  roleName: string;
  dataScope: string;
  memberCount: number;
  updatedAt: string;
  status: string;
}

const columns: ColumnsType<RoleRecord> = [
  { title: '角色名称', dataIndex: 'roleName' },
  { title: '数据范围', dataIndex: 'dataScope' },
  { title: '成员数', dataIndex: 'memberCount' },
  { title: '更新时间', dataIndex: 'updatedAt' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function RolesPage() {
  const { records, loading } = useModuleRecords(fetchRoles, '角色');
  const memberCount = records.reduce((sum, record) => sum + record.memberCount, 0);

  return (
    <ModulePage<RoleRecord>
      eyebrow="系统管理"
      title="角色与数据范围"
      description="先把角色列表、数据范围和启停状态搭清楚。"
      metrics={[
        { label: '角色数', value: String(records.length), hint: '来自后端角色接口' },
        { label: '成员数', value: String(memberCount), hint: '汇总当前角色成员' },
        { label: '启用角色', value: String(records.filter((record) => record.status === '启用').length), hint: '按状态实时统计' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="角色列表"
      searchPlaceholder="搜索角色名称或数据范围"
      getSearchText={(record) => `${record.roleName} ${record.dataScope} ${record.status}`}
    />
  );
}

export default RolesPage;
