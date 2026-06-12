import { Card, Space, Statistic, Typography } from "antd";
import type { ReactNode } from "react";

type MetricCardProps = {
  title: string;
  value: string | number;
  suffix?: string;
  icon?: ReactNode;
  note: string;
};

export function MetricCard({ title, value, suffix, icon, note }: MetricCardProps) {
  return (
    <Card className="metric-card">
      <Space direction="vertical" size={12} style={{ width: "100%" }}>
        <div className="metric-topline">
          <Typography.Text>{title}</Typography.Text>
          <span className="metric-icon">{icon}</span>
        </div>
        <Statistic value={value} suffix={suffix} />
        <Typography.Text type="secondary">{note}</Typography.Text>
      </Space>
    </Card>
  );
}
