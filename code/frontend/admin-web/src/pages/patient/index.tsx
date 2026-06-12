import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchPatients } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface PatientRecord {
  key: string;
  patientName: string;
  gender: string;
  age: number;
  riskLevel: string;
  phone: string;
  lastVisit: string;
}

const columns: ColumnsType<PatientRecord> = [
  { title: '患者姓名', dataIndex: 'patientName' },
  { title: '性别', dataIndex: 'gender' },
  { title: '年龄', dataIndex: 'age' },
  { title: '风险等级', dataIndex: 'riskLevel', render: (value: string) => <Tag color="orange">{value}</Tag> },
  { title: '联系电话', dataIndex: 'phone' },
  { title: '最近就诊', dataIndex: 'lastVisit' },
];

function PatientPage() {
  const { records, loading } = useModuleRecords(fetchPatients, '患者');
  const highRiskCount = records.filter((record) => record.riskLevel.includes('高')).length;

  return (
    <ModulePage<PatientRecord>
      eyebrow="患者中心"
      title="患者档案与风险分层"
      description="承接患者基础档案、风险等级与最近就诊时间。"
      metrics={[
        { label: '在管患者', value: String(records.length), hint: '来自后端患者接口' },
        { label: '高风险患者', value: String(highRiskCount), hint: '按风险等级实时统计' },
        { label: '待随访', value: String(records.length - highRiskCount), hint: '建议按最近就诊排序' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="患者列表"
      searchPlaceholder="搜索患者姓名、电话或风险等级"
      getSearchText={(record) => `${record.patientName} ${record.phone} ${record.riskLevel}`}
    />
  );
}

export default PatientPage;
