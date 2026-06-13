import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchDicts } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface DictRecord {
  key: string;
  dictType: string;
  dictLabel: string;
  dictValue: string;
  sort: number;
  status: string;
  remark: string;
}

const columns: ColumnsType<DictRecord> = [
  { title: '字典类型', dataIndex: 'dictType' },
  { title: '字典标签', dataIndex: 'dictLabel' },
  { title: '字典键值', dataIndex: 'dictValue' },
  { title: '排序', dataIndex: 'sort' },
  { title: '备注', dataIndex: 'remark' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function DictsPage() {
  const { records, loading } = useModuleRecords(fetchDicts, '字典');
  const dictTypeCount = new Set(records.map((record) => record.dictType)).size;

  return (
    <ModulePage<DictRecord>
      eyebrow="系统管理"
      title="字典管理"
      description="统一维护账号状态、菜单类型和业务枚举，减少页面与服务中的硬编码。"
      metrics={[
        { label: '字典项', value: String(records.length), hint: '来自后端字典接口' },
        { label: '字典类型', value: String(dictTypeCount), hint: '按 dictType 聚合' },
        { label: '启用项', value: String(records.filter((record) => record.status === '启用').length), hint: '按状态实时统计' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="字典项列表"
      searchPlaceholder="搜索类型、标签或键值"
      getSearchText={(record) => `${record.dictType} ${record.dictLabel} ${record.dictValue} ${record.remark}`}
    />
  );
}

export default DictsPage;
