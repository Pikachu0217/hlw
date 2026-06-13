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
      <Space direction="vertical" size={20} className="stack-full">
        <div>
          <Typography.Title level={3} className="section-title">
            {title}
          </Typography.Title>
          <Typography.Paragraph type="secondary" className="section-description">
            {description}
          </Typography.Paragraph>
        </div>
        {children}
      </Space>
    </Card>
  );
}
