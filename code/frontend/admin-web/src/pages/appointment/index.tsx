import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ModulePage from '@/components/ModulePage';

interface AppointmentRecord {
  key: string;
  appointmentNo: string;
  patientName: string;
  doctorName: string;
  clinicTime: string;
  source: string;
  status: string;
}

const dataSource: AppointmentRecord[] = [
  { key: '1', appointmentNo: 'YY20260612001', patientName: '赵晓岚', doctorName: '陈知衡', clinicTime: '今天 14:00', source: '小程序', status: '待就诊' },
  { key: '2', appointmentNo: 'YY20260612002', patientName: '沈博远', doctorName: '顾清和', clinicTime: '今天 15:30', source: '客服代约', status: '已签到' },
  { key: '3', appointmentNo: 'YY20260612003', patientName: '林芷言', doctorName: '陆安禾', clinicTime: '明天 09:20', source: '电话预约', status: '已确认' },
];

const columns: ColumnsType<AppointmentRecord> = [
  { title: '预约单号', dataIndex: 'appointmentNo' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '医生', dataIndex: 'doctorName' },
  { title: '门诊时间', dataIndex: 'clinicTime' },
  { title: '来源', dataIndex: 'source' },
  {
    title: '状态',
    dataIndex: 'status',
    render: (value: string) => <Tag color={value === '待就诊' ? 'orange' : value === '已签到' ? 'green' : 'blue'}>{value}</Tag>,
  },
];

// 渲染预约管理基础页。
function AppointmentPage() {
  return (
    <ModulePage<AppointmentRecord>
      eyebrow="预约管理"
      title="门诊预约排期"
      description="围绕预约来源、门诊时间与执行状态形成基础列表，为后续排班联动和核销流程预留位置。"
      badgeText="门诊时段可扩展"
      metrics={[
        { label: '今日预约', value: '186', hint: '线上预约占 71%' },
        { label: '已签到', value: '53', hint: '午后高峰即将开始' },
        { label: '待确认', value: '9', hint: '需客服补齐信息' },
      ]}
      columns={columns}
      dataSource={dataSource}
      tableTitle="预约列表"
      searchPlaceholder="搜索预约单、患者、医生"
      getSearchText={(record) => `${record.appointmentNo} ${record.patientName} ${record.doctorName} ${record.source}`}
    />
  );
}

export default AppointmentPage;
