import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchConsults } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface ConsultRecord {
  key: string;
  consultNo: string;
  patientName: string;
  doctorName: string;
  channel: string;
  status: string;
  updatedAt: string;
}

const columns: ColumnsType<ConsultRecord> = [
  { title: '咨询单号', dataIndex: 'consultNo' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '接诊医生', dataIndex: 'doctorName' },
  { title: '渠道', dataIndex: 'channel' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="blue">{value}</Tag> },
  { title: '最近更新时间', dataIndex: 'updatedAt' },
];

function ConsultPage() {
  const { records, loading } = useModuleRecords(fetchConsults, '问诊');
  const waitingCount = records.filter((record) => record.status.includes('待')).length;

  return (
    <ModulePage<ConsultRecord>
      eyebrow="咨询中心"
      title="咨询单流转看板"
      description="统一呈现患者、医生、渠道和状态。"
      metrics={[
        { label: '问诊单', value: String(records.length), hint: '来自后端问诊接口' },
        { label: '待处理', value: String(waitingCount), hint: '按状态字段实时统计' },
        { label: '进行中', value: String(records.length - waitingCount), hint: '覆盖图文与视频渠道' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="咨询单列表"
      searchPlaceholder="搜索咨询单、患者、医生"
      getSearchText={(record) => `${record.consultNo} ${record.patientName} ${record.doctorName} ${record.channel}`}
    />
  );
}

export default ConsultPage;
