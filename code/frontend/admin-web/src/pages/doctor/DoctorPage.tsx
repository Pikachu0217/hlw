import { Button, Col, Form, Input, Row, Select, Space, Tag } from "antd";
import { PageSection } from "../../components/PageSection";
import { DoctorList, type DoctorListItem } from "./DoctorList";

const doctors: DoctorListItem[] = [
  { id: 1, name: "李医生", title: "主任医师", consultStatus: "ONLINE" },
  { id: 2, name: "周医生", title: "副主任医师", consultStatus: "BUSY" },
  { id: 3, name: "沈医生", title: "主治医师", consultStatus: "OFFLINE" }
];

export function DoctorPage() {
  return (
    <Space direction="vertical" size={24} style={{ width: "100%" }}>
      <PageSection title="医生中心" description="管理医生档案、排班、挂号费和在线问诊状态。">
        <Row gutter={[16, 16]}>
          <Col xs={24} lg={16}>
            <DoctorList doctors={doctors} />
          </Col>
          <Col xs={24} lg={8}>
            <div className="side-panel">
              <Tag color="cyan">排班策略</Tag>
              <p>当前按职称与科室双层配置挂号费，优先级为科室配置高于医生默认配置。</p>
            </div>
          </Col>
        </Row>
      </PageSection>
      <PageSection
        title="医生筛选"
        description="预留联调入口，用于挂接真实医生搜索与状态过滤。"
        extra={<Button type="primary">新增医生</Button>}
      >
        <Form layout="vertical">
          <Row gutter={16}>
            <Col xs={24} md={8}>
              <Form.Item label="医生姓名">
                <Input placeholder="请输入医生姓名" />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item label="所属科室">
                <Select options={[{ value: "内科", label: "内科" }, { value: "儿科", label: "儿科" }]} />
              </Form.Item>
            </Col>
            <Col xs={24} md={8}>
              <Form.Item label="接诊状态">
                <Select
                  options={[
                    { value: "ONLINE", label: "在线" },
                    { value: "BUSY", label: "忙碌" },
                    { value: "OFFLINE", label: "离线" }
                  ]}
                />
              </Form.Item>
            </Col>
          </Row>
        </Form>
      </PageSection>
    </Space>
  );
}
