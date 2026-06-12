import { Card, Space } from "antd-mobile";
import type { ReactNode } from "react";

type SectionCardProps = {
  title: string;
  description?: string;
  children: ReactNode;
};

export function SectionCard({ title, description, children }: SectionCardProps) {
  return (
    <Card className="section-card">
      <Space direction="vertical" block>
        <div>
          <div className="section-title">{title}</div>
          {description ? <div className="section-description">{description}</div> : null}
        </div>
        {children}
      </Space>
    </Card>
  );
}
