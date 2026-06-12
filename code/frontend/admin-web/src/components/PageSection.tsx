import { Card, Space, Typography } from "antd";
import type { ReactNode } from "react";

type PageSectionProps = {
  title: string;
  description: string;
  extra?: ReactNode;
  children: ReactNode;
};

export function PageSection({ title, description, extra, children }: PageSectionProps) {
  return (
    <Card className="glass-card" extra={extra}>
      <Space direction="vertical" size={20} style={{ width: "100%" }}>
        <div>
          <Typography.Title level={3} style={{ marginBottom: 8 }}>
            {title}
          </Typography.Title>
          <Typography.Paragraph type="secondary" style={{ marginBottom: 0 }}>
            {description}
          </Typography.Paragraph>
        </div>
        {children}
      </Space>
    </Card>
  );
}
