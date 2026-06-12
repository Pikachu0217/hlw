import type { ReactNode } from 'react';
import { Button, Space, Tag, Typography } from 'antd';

interface PageHeroProps {
  eyebrow: string;
  title: string;
  description: string;
  badgeText?: string;
  actions?: ReactNode;
}

function PageHero({ eyebrow, title, description, badgeText, actions }: PageHeroProps) {
  return (
    <section className="page-hero">
      <div className="page-hero__content">
        <Tag className="page-hero__eyebrow" bordered={false}>
          {eyebrow}
        </Tag>
        <Typography.Title level={2} className="page-hero__title">
          {title}
        </Typography.Title>
        <Typography.Paragraph className="page-hero__description">
          {description}
        </Typography.Paragraph>
        <Space wrap>
          <Button type="primary">查看今日任务</Button>
          <Button>导出概览</Button>
          {badgeText ? <Tag color="cyan">{badgeText}</Tag> : null}
        </Space>
      </div>
      <div className="page-hero__aside">
        {actions ?? (
          <div className="page-hero__signal">
            <span className="page-hero__signal-label">控制台状态</span>
            <strong className="page-hero__signal-value">稳态运行</strong>
            <span className="page-hero__signal-text">面向门诊、咨询、处方与订单的一体化管理。</span>
          </div>
        )}
      </div>
    </section>
  );
}

export default PageHero;
