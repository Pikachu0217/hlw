import { Card, Col, Row, Typography } from 'antd';
import PageHero from '@/components/PageHero';
import DoctorList, { type DoctorRecord } from '@/pages/doctor/components/DoctorList';

const doctors: DoctorRecord[] = [
  { key: '1', name: '陈知衡', title: '主任医师', department: '心内科', specialty: '冠脉慢病管理', status: '接诊中', schedule: '上午门诊', patientCount: 16 },
  { key: '2', name: '顾清和', title: '副主任医师', department: '内分泌科', specialty: '糖尿病营养干预', status: '候诊', schedule: '下午门诊', patientCount: 9 },
  { key: '3', name: '陆安禾', title: '主治医师', department: '儿科', specialty: '儿童呼吸道随访', status: '停诊', schedule: '远程会诊', patientCount: 5 },
];

const metrics = [
  { label: '在线医生', value: '18', hint: '含图文与视频接诊' },
  { label: '待排班调整', value: '4', hint: '集中在周末门诊' },
  { label: '今日接诊量', value: '126', hint: '心内科占比最高' },
];

// 渲染医生管理基础页，并复用 DoctorList 组件。
function DoctorPage() {
  return (
    <div className="page-shell">
      <PageHero
        eyebrow="医生管理"
        title="医生名录与排班概览"
        description="医生模块采用独立可复用的 DoctorList 组件，方便后续联调用例、搜索、批量操作与自动化测试。"
        badgeText="DoctorList 已抽离"
      />
      <Row gutter={[18, 18]}>
        {metrics.map((item) => (
          <Col key={item.label} xs={24} md={8}>
            <Card className="metric-card" bordered={false}>
              <span className="metric-card__label">{item.label}</span>
              <strong className="metric-card__value">{item.value}</strong>
              <Typography.Text className="metric-card__hint">{item.hint}</Typography.Text>
            </Card>
          </Col>
        ))}
      </Row>
      <DoctorList doctors={doctors} />
    </div>
  );
}

export default DoctorPage;
