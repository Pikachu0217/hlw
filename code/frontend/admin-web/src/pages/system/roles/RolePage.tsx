import { Card, Col, Row, Space, Tag, Typography } from "antd";
import { PageSection } from "../../../components/PageSection";

const roles = [
  { title: "超级管理员", perms: ["跨租户查看", "平台配置", "审计导出"] },
  { title: "医院管理员", perms: ["医生排班", "预约管理", "药品配置"] },
  { title: "药师", perms: ["处方审核", "发货确认"] }
];

export function RolePage() {
  return (
    <PageSection title="角色权限" description="角色与权限模型可按医院租户粒度裁剪。">
      <Row gutter={[16, 16]}>
        {roles.map((role) => (
          <Col xs={24} lg={8} key={role.title}>
            <Card>
              <Space direction="vertical" size={12}>
                <Typography.Title level={4}>{role.title}</Typography.Title>
                <Space wrap>
                  {role.perms.map((perm) => (
                    <Tag key={perm} color="blue">
                      {perm}
                    </Tag>
                  ))}
                </Space>
              </Space>
            </Card>
          </Col>
        ))}
      </Row>
    </PageSection>
  );
}
