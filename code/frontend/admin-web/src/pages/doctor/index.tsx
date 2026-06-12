import { Card, Col, Row, Typography } from 'antd';
import PageHero from '@/components/PageHero';
import DoctorList, { type DoctorRecord } from '@/pages/doctor/components/DoctorList';

const doctors: DoctorRecord[] = [
  { key: '1', name: '陈知衡', title: '主任医师', department: '心内科', specialty: '冠脉慢病管理', status: '接诊中', schedule: '上午门诊', patientCount: 16 },
  { key: '2', name: '顾清和', title: '副主任医师', department: '内分泌科', specialty: '糖尿病营养干预', status: '候诊', schedule: '下午门诊', patientCount: 9 },
];

function DoctorPage() {
  return (
    <div className="page-shell">
      <PageHero eyebrow="医生管理" title="医生名录与排班概览" description="医生模块采用独立可复用的 DoctorList 组件。" badgeText="DoctorList 已抽离" />
      <Row gutter={[18, 18]}>
        {[
          ['在线医生', '18', '含图文与视频接诊'],
          ['待排班调整', '4', '集中在周末门诊'],
          ['今日接诊量', '126', '心内科占比最高'],
        ].map(([label, value, hint]) => (
          <Col key={label} xs={24} md={8}>
            <Card className="metric-card" bordered={false}>
              <span className="metric-card__label">{label}</span>
              <strong className="metric-card__value">{value}</strong>
              <Typography.Text className="metric-card__hint">{hint}</Typography.Text>
            </Card>
          </Col>
        ))}
      </Row>
      <DoctorList doctors={doctors} />
    </div>
  );
}

export default DoctorPage;
