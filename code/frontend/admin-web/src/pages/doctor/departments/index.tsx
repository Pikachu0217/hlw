import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchDepartments } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface DepartmentRecord {
  key: string;
  id: number;
  name: string;
  doctorCount: number;
  queue: string;
}

const columns: ColumnsType<DepartmentRecord> = [
  { title: '科室名称', dataIndex: 'name' },
  { title: '医生数量', dataIndex: 'doctorCount' },
  { title: '候诊状态', dataIndex: 'queue' },
  { title: '状态', render: () => <Tag color="green">启用</Tag> },
];

function DepartmentsPage() {
  const { records, loading } = useModuleRecords(fetchDepartments, '科室');
  const patientWaiting = records.filter((record) => record.queue.includes('等候')).length;

  return (
    <ModulePage<DepartmentRecord>
      eyebrow="医生管理"
      title="科室管理"
      description="维护科室基础资料、医生数量和候诊状态，承接医生与排班关系。"
      metrics={[
        { label: '科室数', value: String(records.length), hint: '来自医生服务科室接口' },
        { label: '候诊科室', value: String(patientWaiting), hint: '按候诊描述统计' },
        { label: '医生总数', value: String(records.reduce((sum, record) => sum + record.doctorCount, 0)), hint: '汇总当前科室医生数' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="科室列表"
      searchPlaceholder="搜索科室或候诊状态"
      getSearchText={(record) => `${record.name} ${record.queue}`}
    />
  );
}

export default DepartmentsPage;
