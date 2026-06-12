import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchDrugs } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface DrugRecord {
  key: string;
  drugName: string;
  spec: string;
  inventory: number;
  unit: string;
  warningStatus: string;
}

const columns: ColumnsType<DrugRecord> = [
  { title: '药品名称', dataIndex: 'drugName' },
  { title: '规格', dataIndex: 'spec' },
  { title: '库存', dataIndex: 'inventory' },
  { title: '单位', dataIndex: 'unit' },
  { title: '预警状态', dataIndex: 'warningStatus', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function DrugPage() {
  const { records, loading } = useModuleRecords(fetchDrugs, '药品');
  const warningCount = records.filter((record) => record.warningStatus.includes('预警')).length;

  return (
    <ModulePage<DrugRecord>
      eyebrow="药品目录"
      title="药品库存与预警"
      description="先沉淀药品基础目录、规格和库存预警状态。"
      metrics={[
        { label: '在售药品', value: String(records.length), hint: '来自后端药品接口' },
        { label: '库存预警', value: String(warningCount), hint: '按预警状态实时统计' },
        { label: '库存合计', value: String(records.reduce((sum, item) => sum + item.inventory, 0)), hint: '汇总当前列表库存' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="药品列表"
      searchPlaceholder="搜索药品名称、规格或状态"
      getSearchText={(record) => `${record.drugName} ${record.spec} ${record.warningStatus}`}
    />
  );
}

export default DrugPage;
