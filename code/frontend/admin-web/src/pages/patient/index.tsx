import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ModulePage from '@/components/ModulePage';

interface PatientRecord {
  key: string;
  patientName: string;
  gender: string;
  age: number;
  riskLevel: string;
  phone: string;
  lastVisit: string;
}

const dataSource: PatientRecord[] = [
  { key: '1', patientName: '赵晓岚', gender: '女', age: 34, riskLevel: '中风险', phone: '13900001111', lastVisit: '2026-06-11' },
  { key: '2', patientName: '沈博远', gender: '男', age: 58, riskLevel: '高风险', phone: '13900002222', lastVisit: '2026-06-10' },
  { key: '3', patientName: '林芷言', gender: '女', age: 27, riskLevel: '低风险', phone: '13900003333', lastVisit: '2026-06-09' },
];

const columns: ColumnsType<PatientRecord> = [
  { title: '患者姓名', dataIndex: 'patientName' },
  { title: '性别', dataIndex: 'gender' },
  { title: '年龄', dataIndex: 'age' },
  {
    title: '风险等级',
    dataIndex: 'riskLevel',
    render: (value: string) => <Tag color={value === '高风险' ? 'red' : value === '中风险' ? 'orange' : 'green'}>{value}</Tag>,
  },
  { title: '联系电话', dataIndex: 'phone' },
  { title: '最近就诊', dataIndex: 'lastVisit' },
];

// 渲染患者管理基础页。
function PatientPage() {
  return (
    <ModulePage<PatientRecord>
      eyebrow="患者中心"
      title="患者档案与风险分层"
      description="承接患者基础档案、风险等级与最近就诊时间，为后续患者详情、标签体系打底。"
      badgeText="患者画像预留"
      metrics={[
        { label: '在管患者', value: '1,286', hint: '近 30 天新增 82 人' },
        { label: '高风险患者', value: '43', hint: '需重点随访' },
        { label: '今日回访', value: '17', hint: '客服与医生联动中' },
      ]}
      columns={columns}
      dataSource={dataSource}
      tableTitle="患者列表"
      searchPlaceholder="搜索患者姓名、电话或风险等级"
      getSearchText={(record) => `${record.patientName} ${record.phone} ${record.riskLevel}`}
    />
  );
}

export default PatientPage;
