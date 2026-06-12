import {
  Alert,
  Card,
  Col,
  List,
  Progress,
  Row,
  Space,
  Table,
  Tag,
  Typography,
} from 'antd';
import type { ColumnsType } from 'antd/es/table';
import PageHero from '@/components/PageHero';

interface DashboardTask {
  key: string;
  module: string;
  owner: string;
  status: string;
  progress: number;
}

const dashboardMetrics = [
  { label: '今日预约', value: '186', hint: '较昨日 +14 单' },
  { label: '待处理咨询', value: '28', hint: '视频咨询占 39%' },
  { label: '异常订单', value: '6', hint: '需财务与运营复核' },
  { label: '药品预警', value: '11', hint: '库存低于安全线' },
];

const taskData: DashboardTask[] = [
  { key: '1', module: '医生管理', owner: '排班组', status: '排班确认中', progress: 74 },
  { key: '2', module: '咨询单', owner: '客服组', status: '等待医生接单', progress: 56 },
  { key: '3', module: '订单中心', owner: '财务组', status: '退款复核中', progress: 32 },
  { key: '4', module: '药品目录', owner: '药房组', status: '库存补货中', progress: 88 },
];

const taskColumns: ColumnsType<DashboardTask> = [
  { title: '模块', dataIndex: 'module' },
  { title: '责任组', dataIndex: 'owner' },
  {
    title: '当前状态',
    dataIndex: 'status',
    render: (value: string) => <Tag color="blue">{value}</Tag>,
  },
  {
    title: '推进度',
    dataIndex: 'progress',
    render: (value: number) => <Progress percent={value} size="small" strokeColor="#147d8f" />,
  },
];

const quickSignals = [
  '租户续费预警需在本周内完成跟进',
  '门诊高峰时段集中在 14:00 - 17:30',
  '图文咨询平均响应时长已降至 8 分钟',
];

const moduleCards = [
  { title: '租户与套餐', text: '面向多院区统一查看到期、套餐与管理员账号。' },
  { title: '诊疗履约', text: '围绕咨询、预约、处方、订单形成闭环看板。' },
  { title: '基础配置', text: '用户、角色、菜单均已预留扩展位，便于接真接口。' },
];

// 渲染管理端首页总览。
function DashboardPage() {
  return (
    <div className="page-shell">
      <PageHero
        eyebrow="运营总览"
        title="今日医疗运营脉冲"
        description="把租户、诊疗与履约的关键指标收拢到同一张控制台里，帮助值班人员快速定位风险与优先级。"
        badgeText="控制台已就绪"
        actions={
          <div className="page-hero__signal">
            <span className="page-hero__signal-label">今日重点</span>
            <strong className="page-hero__signal-value">先处理预约和库存</strong>
            <span className="page-hero__signal-text">预约高峰与药品预警同时抬头，建议优先协调门诊与药房。</span>
          </div>
        }
      />
      <Row gutter={[18, 18]}>
        {dashboardMetrics.map((item) => (
          <Col key={item.label} xs={24} md={12} xl={6}>
            <Card className="metric-card" bordered={false}>
              <span className="metric-card__label">{item.label}</span>
              <strong className="metric-card__value">{item.value}</strong>
              <Typography.Text className="metric-card__hint">{item.hint}</Typography.Text>
            </Card>
          </Col>
        ))}
      </Row>
      <Row gutter={[18, 18]}>
        <Col xs={24} xl={16}>
          <Card className="console-card" bordered={false}>
            <div className="console-card__toolbar">
              <div>
                <Typography.Title level={4} className="console-card__title">
                  今日任务推进
                </Typography.Title>
                <Typography.Text className="console-card__subtitle">可直接替换为真实待办接口。</Typography.Text>
              </div>
              <Tag color="cyan">4 个重点任务</Tag>
            </div>
            <Table<DashboardTask> rowKey="key" columns={taskColumns} dataSource={taskData} pagination={false} />
          </Card>
        </Col>
        <Col xs={24} xl={8}>
          <Card className="console-card console-card--stacked" bordered={false}>
            <Typography.Title level={4} className="console-card__title">
              控制台提醒
            </Typography.Title>
            <Space direction="vertical" size={14} className="dashboard-alerts">
              {quickSignals.map((signal) => (
                <Alert key={signal} message={signal} type="info" showIcon />
              ))}
            </Space>
          </Card>
        </Col>
      </Row>
      <Row gutter={[18, 18]}>
        {moduleCards.map((card) => (
          <Col key={card.title} xs={24} md={8}>
            <Card className="console-card console-card--feature" bordered={false}>
              <Typography.Title level={4} className="console-card__title">
                {card.title}
              </Typography.Title>
              <Typography.Paragraph className="console-card__paragraph">{card.text}</Typography.Paragraph>
            </Card>
          </Col>
        ))}
      </Row>
      <Card className="console-card" bordered={false}>
        <Typography.Title level={4} className="console-card__title">
          最近联动动作
        </Typography.Title>
        <List
          dataSource={[
            '系统管理员已同步新增 2 个门诊医生账号',
            '药房组刚刚完成 4 个紧缺药品的安全库存调整',
            '客服组正在处理 3 条支付失败后的预约补单',
          ]}
          renderItem={(item) => <List.Item>{item}</List.Item>}
        />
      </Card>
    </div>
  );
}

export default DashboardPage;
