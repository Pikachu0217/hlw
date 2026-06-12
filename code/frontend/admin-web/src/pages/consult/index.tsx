import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ModulePage from '@/components/ModulePage';

interface ConsultRecord {
  key: string;
  consultNo: string;
  patientName: string;
  doctorName: string;
  channel: string;
  status: string;
  updatedAt: string;
}

const dataSource: ConsultRecord[] = [
  { key: '1', consultNo: 'ZX20260612001', patientName: '赵晓岚', doctorName: '陈知衡', channel: '图文', status: '待接单', updatedAt: '10:18' },
  { key: '2', consultNo: 'ZX20260612002', patientName: '沈博远', doctorName: '顾清和', channel: '视频', status: '咨询中', updatedAt: '10:07' },
];

const columns: ColumnsType<ConsultRecord> = [
  { title: '咨询单号', dataIndex: 'consultNo' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '接诊医生', dataIndex: 'doctorName' },
  { title: '渠道', dataIndex: 'channel' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="blue">{value}</Tag> },
  { title: '最近更新时间', dataIndex: 'updatedAt' },
];

function ConsultPage() {
  return (
    <ModulePage<ConsultRecord>
      eyebrow="咨询中心"
      title="咨询单流转看板"
      description="统一呈现患者、医生、渠道和状态。"
      metrics={[
        { label: '待接单', value: '12', hint: '视频咨询 5 单' },
        { label: '咨询中', value: '8', hint: '平均等待 6 分钟' },
        { label: '今日完结', value: '49', hint: '满意度 96%' },
      ]}
      columns={columns}
      dataSource={dataSource}
      tableTitle="咨询单列表"
      searchPlaceholder="搜索咨询单、患者、医生"
      getSearchText={(record) => `${record.consultNo} ${record.patientName} ${record.doctorName} ${record.channel}`}
    />
  );
}

export default ConsultPage;
