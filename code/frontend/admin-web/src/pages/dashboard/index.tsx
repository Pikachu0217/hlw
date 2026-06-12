import { Card, Col, List, Row, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import PageHero from '@/components/PageHero';

interface DashboardTask {
  key: string;
  module: string;
  owner: string;
  status: string;
}

const taskData: DashboardTask[] = [
  { key: '1', module: '医生管理', owner: '排班组', status: '排班确认中' },
  { key: '2', module: '咨询单', owner: '客服组', status: '等待医生接单' },
  { key: '3', module: '订单中心', owner: '财务组', status: '退款复核中' },
];

const taskColumns: ColumnsType<DashboardTask> = [
  { title: '模块', dataIndex: 'module' },
  { title: '责任组', dataIndex: 'owner' },
  { title: '当前状态', dataIndex: 'status', render: (value: string) => <Tag color="blue">{value}</Tag> },
];

function DashboardPage() {
  return (
    <div className="page-shell">
      <PageHero eyebrow="运营总览" title="今日医疗运营脉冲" description="把租户、诊疗与履约的关键指标收拢到同一张控制台里。" badgeText="控制台已就绪" />
      <Row gutter={[18, 18]}>
        {[
          ['今日预约', '186', '较昨日 +14 单'],
          ['待处理咨询', '28', '视频咨询占 39%'],
          ['异常订单', '6', '需财务与运营复核'],
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
      <Card className="console-card" bordered={false}>
        <Table<DashboardTask> rowKey="key" columns={taskColumns} dataSource={taskData} pagination={false} />
      </Card>
      <Card className="console-card" bordered={false}>
        <List
          dataSource={['租户续费预警需在本周内完成跟进', '门诊高峰时段集中在 14:00 - 17:30', '图文咨询平均响应时长已降至 8 分钟']}
          renderItem={(item) => <List.Item>{item}</List.Item>}
        />
      </Card>
    </div>
  );
}

export default DashboardPage;
