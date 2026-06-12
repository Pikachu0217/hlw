import { Card, Col, Row, Spin, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { fetchDoctors } from '@/api/doctor';
import PageHero from '@/components/PageHero';
import DoctorList, { type DoctorRecord } from '@/pages/doctor/components/DoctorList';

function DoctorPage() {
  const [doctors, setDoctors] = useState<DoctorRecord[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    fetchDoctors()
      .then((records) => {
        if (!ignore) {
          setDoctors(records);
        }
      })
      .catch(() => {
        message.warning('医生服务暂不可用，请稍后重试');
      })
      .finally(() => {
        if (!ignore) {
          setLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  return (
    <div className="page-shell">
      <PageHero eyebrow="医生管理" title="医生名录与排班概览" description="医生模块采用独立可复用的 DoctorList 组件。" badgeText="DoctorList 已抽离" />
      <Row gutter={[18, 18]}>
        {[
          ['医生总数', String(doctors.length), '来自后端医生接口'],
          ['接诊医生', String(doctors.filter((doctor) => doctor.status.includes('接诊')).length), '按接诊状态实时统计'],
          ['今日接诊量', String(doctors.reduce((sum, doctor) => sum + doctor.patientCount, 0)), '汇总当前医生接诊数'],
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
      <Spin spinning={loading}>
        <DoctorList doctors={doctors} />
      </Spin>
    </div>
  );
}

export default DoctorPage;
