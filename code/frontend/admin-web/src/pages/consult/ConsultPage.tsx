import { Col, List, Row, Tag, Typography } from "antd";
import { PageSection } from "../../components/PageSection";

export function ConsultPage() {
  return (
    <PageSection title="在线问诊" description="聚合展示当前待接诊、进行中与临近超时的问诊单。">
      <Row gutter={[16, 16]}>
        <Col xs={24} md={8}>
          <div className="status-board">
            <Typography.Title level={4}>待接诊</Typography.Title>
            <Typography.Title level={2}>14</Typography.Title>
            <Tag color="orange">15 分钟内需处理</Tag>
          </div>
        </Col>
        <Col xs={24} md={8}>
          <div className="status-board">
            <Typography.Title level={4}>进行中</Typography.Title>
            <Typography.Title level={2}>23</Typography.Title>
            <Tag color="cyan">图文问诊为主</Tag>
          </div>
        </Col>
        <Col xs={24} md={8}>
          <div className="status-board">
            <Typography.Title level={4}>超时预警</Typography.Title>
            <Typography.Title level={2}>5</Typography.Title>
            <Tag color="red">需优先跟进</Tag>
          </div>
        </Col>
      </Row>
      <List
        className="spaced-list"
        dataSource={["李医生 - 儿科图文问诊 - 剩余 08:30", "周医生 - 内科复诊续方 - 剩余 04:50"]}
        renderItem={(item) => <List.Item>{item}</List.Item>}
      />
    </PageSection>
  );
}
