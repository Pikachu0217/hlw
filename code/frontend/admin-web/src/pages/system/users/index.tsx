import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchUsers } from '@/api/modules';
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
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function UsersPage() {
  const { records, loading } = useModuleRecords(fetchUsers, '用户');

  return (
    <ModulePage<UserRecord>
      eyebrow="系统管理"
      title="后台用户清单"
      description="沉淀账号、角色、部门和登录信息。"
      metrics={[
        { label: '启用账号', value: String(records.filter((record) => record.status === '启用').length), hint: '来自后端用户接口' },
        { label: '用户总数', value: String(records.length), hint: '覆盖当前后台账号' },
        { label: '业务组', value: String(new Set(records.map((record) => record.deptName)).size), hint: '按部门字段实时统计' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="用户列表"
      searchPlaceholder="搜索账号、部门、角色"
      getSearchText={(record) => `${record.username} ${record.deptName} ${record.roleName} ${record.phone}`}
    />
  );
}

export default UsersPage;
