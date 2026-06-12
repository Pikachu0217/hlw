import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchTenants } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface TenantRecord {
  key: string;
  tenantName: string;
  packageName: string;
  adminName: string;
  expireAt: string;
  status: string;
}

const columns: ColumnsType<TenantRecord> = [
  { title: '租户名称', dataIndex: 'tenantName' },
  { title: '套餐版本', dataIndex: 'packageName' },
  { title: '管理员', dataIndex: 'adminName' },
  { title: '到期时间', dataIndex: 'expireAt' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '正常' ? 'green' : 'blue'}>{value}</Tag> },
];

function TenantPage() {
  const { records, loading } = useModuleRecords(fetchTenants, '租户');
  const warningCount = records.filter((record) => record.status !== '正常').length;

  return (
    <ModulePage<TenantRecord>
      eyebrow="租户中心"
      title="多租户运营总览"
      description="围绕套餐、管理员与到期时间搭建租户基础运营面板。"
      badgeText={`${records.length} 个租户`}
      metrics={[
        { label: '活跃租户', value: String(records.length - warningCount), hint: '来自后端租户接口' },
        { label: '续费跟进', value: String(warningCount), hint: '按租户状态实时统计' },
        { label: '租户总数', value: String(records.length), hint: '覆盖当前系统租户' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="租户列表"
      searchPlaceholder="搜索租户、管理员或套餐"
      getSearchText={(record) => `${record.tenantName} ${record.adminName} ${record.packageName} ${record.status}`}
    />
  );
}

export default TenantPage;
