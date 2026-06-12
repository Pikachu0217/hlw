import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchAppointments } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface AppointmentRecord {
  key: string;
  appointmentNo: string;
  patientName: string;
  doctorName: string;
  clinicTime: string;
  source: string;
  status: string;
}

const columns: ColumnsType<AppointmentRecord> = [
  { title: '预约单号', dataIndex: 'appointmentNo' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '医生', dataIndex: 'doctorName' },
  { title: '门诊时间', dataIndex: 'clinicTime' },
  { title: '来源', dataIndex: 'source' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function AppointmentPage() {
  const { records, loading } = useModuleRecords(fetchAppointments, '预约');
  const checkedInCount = records.filter((record) => record.status.includes('签到')).length;

  return (
    <ModulePage<AppointmentRecord>
      eyebrow="预约管理"
      title="门诊预约排期"
      description="围绕预约来源、门诊时间与执行状态形成基础列表。"
      metrics={[
        { label: '今日预约', value: String(records.length), hint: '来自后端预约接口' },
        { label: '已签到', value: String(checkedInCount), hint: '按接口状态实时统计' },
        { label: '待处理', value: String(records.length - checkedInCount), hint: '需继续跟进就诊状态' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="预约列表"
      searchPlaceholder="搜索预约单、患者、医生"
      getSearchText={(record) => `${record.appointmentNo} ${record.patientName} ${record.doctorName} ${record.source}`}
    />
  );
}

export default AppointmentPage;
