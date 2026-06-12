import { Col, Progress, Row, Table } from "antd";
import { PageSection } from "../../components/PageSection";

const stockData = [
  { id: 1, name: "阿莫西林胶囊", stock: 320, delivery: "今日两波次" },
  { id: 2, name: "布洛芬混悬液", stock: 86, delivery: "库存预警" }
];

export function DrugPage() {
  return (
    <PageSection title="药品库存" description="用于管理药品目录、库存状态和配送节奏。">
      <Row gutter={[16, 16]}>
        <Col xs={24} xl={10}>
          <div className="side-panel">
            当日发货达成度
            <Progress percent={74} strokeColor="#0f8fa8" />
          </div>
        </Col>
        <Col xs={24} xl={14}>
          <Table
            rowKey="id"
            pagination={false}
            dataSource={stockData}
            columns={[
              { title: "药品名称", dataIndex: "name" },
              { title: "当前库存", dataIndex: "stock" },
              { title: "配送状态", dataIndex: "delivery" }
            ]}
          />
        </Col>
      </Row>
    </PageSection>
  );
}
