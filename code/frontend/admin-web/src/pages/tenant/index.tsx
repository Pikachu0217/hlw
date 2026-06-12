import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ModulePage from '@/components/ModulePage';

interface TenantRecord {
  key: string;
  tenantName: string;
  packageName: string;
  adminName: string;
  expireAt: string;
  status: string;
}

const dataSource: TenantRecord[] = [
  { key: '1', tenantName: '海岚门诊', packageName: '标准医疗版', adminName: '刘院长', expireAt: '2026-12-31', status: '正常' },
  { key: '2', tenantName: '青禾互联网医院', packageName: '集团旗舰版', adminName: '姜主任', expireAt: '2026-08-16', status: '续费跟进' },
  { key: '3', tenantName: '云舟专科中心', packageName: '专科增强版', adminName: '沈经理', expireAt: '2026-07-05', status: '即将到期' },
];

const columns: ColumnsType<TenantRecord> = [
  { title: '租户名称', dataIndex: 'tenantName' },
  { title: '套餐版本', dataIndex: 'packageName' },
  { title: '管理员', dataIndex: 'adminName' },
  { title: '到期时间', dataIndex: 'expireAt' },
  {
    title: '状态',
    dataIndex: 'status',
    render: (value: string) => <Tag color={value === '正常' ? 'green' : value === '续费跟进' ? 'blue' : 'orange'}>{value}</Tag>,
  },
];

// 渲染租户管理基础页。
function TenantPage() {
  return (
    <ModulePage<TenantRecord>
      eyebrow="租户中心"
      title="多租户运营总览"
      description="围绕套餐、管理员与到期时间搭建租户基础运营面板，为后续接入真实租户接口预留结构。"
      badgeText="3 个租户样例"
      metrics={[
        { label: '活跃租户', value: '32', hint: '集团版占 28%' },
        { label: '本月续费', value: '7', hint: '含 2 家重点跟进' },
        { label: '到期预警', value: '3', hint: '30 天内即将到期' },
      ]}
      columns={columns}
      dataSource={dataSource}
      tableTitle="租户列表"
      searchPlaceholder="搜索租户、管理员或套餐"
      getSearchText={(record) => `${record.tenantName} ${record.adminName} ${record.packageName} ${record.status}`}
    />
  );
}

export default TenantPage;
