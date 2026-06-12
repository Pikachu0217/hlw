import { SearchOutlined } from '@ant-design/icons';
import { Button, Card, Col, Input, Row, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import PageHero from '@/components/PageHero';

interface MetricCardItem {
  label: string;
  value: string;
  hint: string;
}

interface ModulePageProps<T extends { key: string }> {
  eyebrow: string;
  title: string;
  description: string;
  badgeText?: string;
  metrics: MetricCardItem[];
  columns: ColumnsType<T>;
  dataSource: T[];
  loading?: boolean;
  tableTitle: string;
  searchPlaceholder: string;
  getSearchText: (record: T) => string;
}

function ModulePage<T extends { key: string }>({
  eyebrow,
  title,
  description,
  badgeText,
  metrics,
  columns,
  dataSource,
  loading = false,
  tableTitle,
  searchPlaceholder,
  getSearchText,
}: ModulePageProps<T>) {
  const [keyword, setKeyword] = useState('');

  const filteredData = keyword.trim()
    ? dataSource.filter((record) => getSearchText(record).toLowerCase().includes(keyword.trim().toLowerCase()))
    : dataSource;

  return (
    <div className="page-shell">
      <PageHero eyebrow={eyebrow} title={title} description={description} badgeText={badgeText} />
      <Row gutter={[18, 18]}>
        {metrics.map((item) => (
          <Col key={item.label} xs={24} md={8}>
            <Card className="metric-card" bordered={false}>
              <span className="metric-card__label">{item.label}</span>
              <strong className="metric-card__value">{item.value}</strong>
              <Typography.Text className="metric-card__hint">{item.hint}</Typography.Text>
            </Card>
          </Col>
        ))}
      </Row>
      <Card className="console-card" bordered={false}>
        <div className="console-card__toolbar">
          <div>
            <Typography.Title level={4} className="console-card__title">
              {tableTitle}
            </Typography.Title>
            <Typography.Text className="console-card__subtitle">适合后续接入真实接口与分页查询。</Typography.Text>
          </div>
          <Space wrap>
            <Input
              allowClear
              value={keyword}
              onChange={(event) => setKeyword(event.target.value)}
              prefix={<SearchOutlined />}
              placeholder={searchPlaceholder}
              className="console-card__search"
            />
            <Button type="primary">新增</Button>
            <Button>批量导出</Button>
            <Tag color="blue">当前 {filteredData.length} 条</Tag>
          </Space>
        </div>
        <Table<T> rowKey="key" columns={columns} dataSource={filteredData} loading={loading} pagination={false} />
      </Card>
    </div>
  );
}

export default ModulePage;
