import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ModulePage from '@/components/ModulePage';

interface DrugRecord {
  key: string;
  drugName: string;
  spec: string;
  inventory: number;
  unit: string;
  warningStatus: string;
}

const dataSource: DrugRecord[] = [
  { key: '1', drugName: '阿托伐他汀钙片', spec: '20mg*14片', inventory: 124, unit: '盒', warningStatus: '正常' },
  { key: '2', drugName: '盐酸二甲双胍缓释片', spec: '0.5g*30片', inventory: 42, unit: '盒', warningStatus: '预警' },
];

const columns: ColumnsType<DrugRecord> = [
  { title: '药品名称', dataIndex: 'drugName' },
  { title: '规格', dataIndex: 'spec' },
  { title: '库存', dataIndex: 'inventory' },
  { title: '单位', dataIndex: 'unit' },
  { title: '预警状态', dataIndex: 'warningStatus', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function DrugPage() {
  return (
    <ModulePage<DrugRecord>
      eyebrow="药品目录"
      title="药品库存与预警"
      description="先沉淀药品基础目录、规格和库存预警状态。"
      metrics={[
        { label: '在售药品', value: '328', hint: '口服药占比 58%' },
        { label: '库存预警', value: '11', hint: '需 24 小时内补货' },
        { label: '紧缺药品', value: '3', hint: '建议优先协调采购' },
      ]}
      columns={columns}
      dataSource={dataSource}
      tableTitle="药品列表"
      searchPlaceholder="搜索药品名称、规格或状态"
      getSearchText={(record) => `${record.drugName} ${record.spec} ${record.warningStatus}`}
    />
  );
}

export default DrugPage;
