import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchPrescriptions } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface PrescriptionRecord {
  key: string;
  prescriptionNo: string;
  patientName: string;
  doctorName: string;
  drugCount: number;
  issuedAt: string;
  status: string;
}

const columns: ColumnsType<PrescriptionRecord> = [
  { title: '处方编号', dataIndex: 'prescriptionNo' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '开方医生', dataIndex: 'doctorName' },
  { title: '药品数', dataIndex: 'drugCount' },
  { title: '开立时间', dataIndex: 'issuedAt' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="orange">{value}</Tag> },
];

function PrescriptionPage() {
  const { records, loading } = useModuleRecords(fetchPrescriptions, '处方');
  const auditCount = records.filter((record) => record.status.includes('审')).length;

  return (
    <ModulePage<PrescriptionRecord>
      eyebrow="处方中心"
      title="处方流转与审方准备"
      description="以处方编号、患者、医生和当前状态为核心。"
      metrics={[
        { label: '处方数', value: String(records.length), hint: '来自后端处方接口' },
        { label: '待审方', value: String(auditCount), hint: '按处方状态实时统计' },
        { label: '待发药', value: String(records.length - auditCount), hint: '可继续对接药房履约' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="处方列表"
      searchPlaceholder="搜索处方编号、患者或医生"
      getSearchText={(record) => `${record.prescriptionNo} ${record.patientName} ${record.doctorName} ${record.status}`}
    />
  );
}

export default PrescriptionPage;
