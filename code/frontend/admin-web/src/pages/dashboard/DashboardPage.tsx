import { CalendarOutlined, MessageOutlined, RiseOutlined, ShoppingCartOutlined } from "@ant-design/icons";
import { Col, List, Row, Space, Tag, Timeline, Typography } from "antd";
import { MetricCard } from "../../components/MetricCard";
import { PageSection } from "../../components/PageSection";

const todoItems = [
  "补齐医院租户续费提醒",
  "接入真实处方审核列表数据",
  "完成预约号源日历联调"
];

export function DashboardPage() {
  return (
    <Space direction="vertical" size={24} className="stack-full">
      <Row gutter={[16, 16]}>
        <Col xs={24} md={12} xl={6}>
          <MetricCard title="今日问诊" value={126} suffix="单" icon={<MessageOutlined />} note="较昨日提升 18%" />
        </Col>
        <Col xs={24} md={12} xl={6}>
          <MetricCard title="预约支付" value={92} suffix="单" icon={<CalendarOutlined />} note="上午号源热度最高" />
        </Col>
        <Col xs={24} md={12} xl={6}>
          <MetricCard title="处方审核" value={38} suffix="单" icon={<RiseOutlined />} note="药师平均处理 6 分钟" />
        </Col>
        <Col xs={24} md={12} xl={6}>
          <MetricCard title="订单发货" value={54} suffix="单" icon={<ShoppingCartOutlined />} note="配送准时率 98%" />
        </Col>
      </Row>
      <Row gutter={[16, 16]}>
        <Col xs={24} xl={15}>
          <PageSection title="运营节奏" description="围绕预约、问诊、审核、发货四条主链路展示当前运营概况。">
            <Timeline
              items={[
                { children: "08:30 晨间门诊放号完成，新增 240 个号源。" },
                { children: "10:20 图文问诊进入高峰，待分诊问诊单 14 个。" },
                { children: "13:40 药师审核队列清空，开始处理复诊续方。" },
                { children: "16:10 当日药品配送波次已完成 2/3。" }
              ]}
            />
          </PageSection>
        </Col>
        <Col xs={24} xl={9}>
          <PageSection title="本周待办" description="这些事项通常需要医院运营或产品侧优先处理。">
            <List
              dataSource={todoItems}
              renderItem={(item) => (
                <List.Item>
                  <Space>
                    <Tag color="blue">待办</Tag>
                    <Typography.Text>{item}</Typography.Text>
                  </Space>
                </List.Item>
              )}
            />
          </PageSection>
        </Col>
      </Row>
    </Space>
  );
}
