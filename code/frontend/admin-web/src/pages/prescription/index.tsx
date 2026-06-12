import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ModulePage from '@/components/ModulePage';

interface PrescriptionRecord {
  key: string;
  prescriptionNo: string;
  patientName: string;
  doctorName: string;
  drugCount: number;
  issuedAt: string;
  status: string;
}

const dataSource: PrescriptionRecord[] = [
  { key: '1', prescriptionNo: 'CF20260612001', patientName: '赵晓岚', doctorName: '陈知衡', drugCount: 3, issuedAt: '09:42', status: '待审方' },
  { key: '2', prescriptionNo: 'CF20260612002', patientName: '沈博远', doctorName: '顾清和', drugCount: 5, issuedAt: '09:18', status: '待发药' },
];

const columns: ColumnsType<PrescriptionRecord> = [
  { title: '处方编号', dataIndex: 'prescriptionNo' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '开方医生', dataIndex: 'doctorName' },
  { title: '药品数', dataIndex: 'drugCount' },
  { title: '开立时间', dataIndex: 'issuedAt' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="orange">{value}</Tag> },
];

function PrescriptionPage() {
  return (
    <ModulePage<PrescriptionRecord>
      eyebrow="处方中心"
      title="处方流转与审方准备"
      description="以处方编号、患者、医生和当前状态为核心。"
      metrics={[
        { label: '待审方', value: '14', hint: '需药师优先处理' },
        { label: '待发药', value: '21', hint: '药房正在配药' },
        { label: '今日完成', value: '63', hint: '平均流转 18 分钟' },
      ]}
      columns={columns}
      dataSource={dataSource}
      tableTitle="处方列表"
      searchPlaceholder="搜索处方编号、患者或医生"
      getSearchText={(record) => `${record.prescriptionNo} ${record.patientName} ${record.doctorName} ${record.status}`}
    />
  );
}

export default PrescriptionPage;
