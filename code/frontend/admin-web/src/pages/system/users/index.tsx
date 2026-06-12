import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ModulePage from '@/components/ModulePage';

interface UserRecord {
  key: string;
  username: string;
  deptName: string;
  roleName: string;
  phone: string;
  lastLogin: string;
  status: string;
}

const dataSource: UserRecord[] = [
  { key: '1', username: '门诊运营', deptName: '运营中心', roleName: '运营管理员', phone: '13800001111', lastLogin: '今天 08:40', status: '启用' },
  { key: '2', username: '药房主管', deptName: '药房组', roleName: '库存专员', phone: '13800002222', lastLogin: '今天 07:58', status: '启用' },
];

const columns: ColumnsType<UserRecord> = [
  { title: '账号名称', dataIndex: 'username' },
  { title: '部门', dataIndex: 'deptName' },
  { title: '角色', dataIndex: 'roleName' },
  { title: '联系电话', dataIndex: 'phone' },
  { title: '最近登录', dataIndex: 'lastLogin' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function UsersPage() {
  return (
    <ModulePage<UserRecord>
      eyebrow="系统管理"
      title="后台用户清单"
      description="沉淀账号、角色、部门和登录信息。"
      metrics={[
        { label: '启用账号', value: '48', hint: '覆盖 6 个业务组' },
        { label: '今日登录', value: '19', hint: '高峰出现在 8:00 - 9:00' },
        { label: '停用账号', value: '5', hint: '等待权限回收' },
      ]}
      columns={columns}
      dataSource={dataSource}
      tableTitle="用户列表"
      searchPlaceholder="搜索账号、部门、角色"
      getSearchText={(record) => `${record.username} ${record.deptName} ${record.roleName} ${record.phone}`}
    />
  );
}

export default UsersPage;
