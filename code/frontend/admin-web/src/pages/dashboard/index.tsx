import { Card, Col, List, Row, Table, Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useEffect, useState } from 'react';
import { fetchAppointments, fetchConsults, fetchOrders } from '@/api/modules';
import { MetricCard } from '@/components/MetricCard';
import PageHero from '@/components/PageHero';

interface DashboardTask {
  id: string;
  module: string;
  owner: string;
  status: string;
}

const taskColumns: ColumnsType<DashboardTask> = [
  { title: '模块', dataIndex: 'module' },
  { title: '责任组', dataIndex: 'owner' },
  { title: '当前状态', dataIndex: 'status', render: (value: string) => <Tag color="blue">{value}</Tag> },
];

function DashboardPage() {
  const [taskData, setTaskData] = useState<DashboardTask[]>([]);
  const [metrics, setMetrics] = useState([
    ['今日预约', '0', '来自后端预约接口'],
    ['待处理咨询', '0', '来自后端问诊接口'],
    ['待支付订单', '0', '来自后端订单接口'],
  ]);
  const [notices, setNotices] = useState<string[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    Promise.all([fetchAppointments(), fetchConsults(), fetchOrders()])
      .then(([appointments, consults, orders]) => {
        if (ignore) {
          return;
        }

        const waitingConsultCount = consults.filter((consult) => consult.status.includes('待')).length;
        const pendingOrderCount = orders.filter((order) => order.payStatus.includes('待')).length;

        setMetrics([
          ['今日预约', String(appointments.length), '来自后端预约接口'],
          ['待处理咨询', String(waitingConsultCount), '按问诊状态实时统计'],
          ['待支付订单', String(pendingOrderCount), '按订单支付状态实时统计'],
        ]);
        setTaskData([
          { id: 'appointment', module: '预约管理', owner: '门诊运营组', status: `${appointments.length} 单待跟进` },
          { id: 'consult', module: '咨询单', owner: '客服组', status: `${waitingConsultCount} 单待处理` },
          { id: 'order', module: '订单中心', owner: '财务组', status: `${pendingOrderCount} 单待支付` },
        ]);
        setNotices([
          `预约接口返回 ${appointments.length} 条记录`,
          `问诊接口返回 ${consults.length} 条记录`,
          `订单接口返回 ${orders.length} 条记录`,
        ]);
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
      <PageHero eyebrow="运营总览" title="今日医疗运营脉冲" description="把租户、诊疗与履约的关键指标收拢到同一张控制台里。" badgeText="控制台已就绪" />
      <Row gutter={[18, 18]}>
        {metrics.map(([label, value, hint]) => (
          <Col key={label} xs={24} md={8}>
            <MetricCard title={label} value={value} note={hint} />
          </Col>
        ))}
      </Row>
      <Card className="console-card" bordered={false}>
        <Table<DashboardTask> rowKey="id" columns={taskColumns} dataSource={taskData} loading={loading} pagination={false} />
      </Card>
      <Card className="console-card" bordered={false}>
        <List
          dataSource={notices}
          renderItem={(item) => <List.Item>{item}</List.Item>}
        />
      </Card>
    </div>
  );
}

export default DashboardPage;
