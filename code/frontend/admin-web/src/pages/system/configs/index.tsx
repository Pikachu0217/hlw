import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchConfigs } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface ConfigRecord {
  key: string;
  configKey: string;
  configValue: string;
  configType: string;
  status: string;
  remark: string;
}

const columns: ColumnsType<ConfigRecord> = [
  { title: '配置键', dataIndex: 'configKey' },
  { title: '配置值', dataIndex: 'configValue' },
  { title: '配置类型', dataIndex: 'configType' },
  { title: '备注', dataIndex: 'remark' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '启用' : '禁用'}</Tag> },
];

function ConfigsPage() {
  const { records, loading } = useModuleRecords(fetchConfigs, '参数配置');

  return (
    <ModulePage<ConfigRecord>
      eyebrow="系统管理"
      title="参数配置"
      description="集中沉淀问诊时长、放号窗口、安全策略等可运营参数。"
      metrics={[
        { label: '配置项', value: String(records.length), hint: '来自后端配置接口' },
        { label: '配置类型', value: String(new Set(records.map((record) => record.configType)).size), hint: '按配置类型聚合' },
        { label: '启用配置', value: String(records.filter((record) => record.status === '0').length), hint: '按状态实时统计' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="参数配置列表"
      searchPlaceholder="搜索配置键、类型或备注"
      getSearchText={(record) => `${record.configKey} ${record.configType} ${record.configValue} ${record.remark}`}
    />
  );
}

export default ConfigsPage;
