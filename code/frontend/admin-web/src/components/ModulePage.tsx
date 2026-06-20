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

interface ModulePageProps<T extends { id: string | number }> {
  /** 页面眉标。 */
  eyebrow: string;
  /** 页面标题。 */
  title: string;
  /** 页面说明。 */
  description: string;
  /** 页面徽标文案。 */
  badgeText?: string;
  /** 指标卡片。 */
  metrics: MetricCardItem[];
  /** 表格列配置。 */
  columns: ColumnsType<T>;
  /** 表格数据。 */
  dataSource: T[];
  /** 加载状态。 */
  loading?: boolean;
  /** 表格标题。 */
  tableTitle: string;
  /** 搜索占位文案。 */
  searchPlaceholder: string;
  /** 搜索文本提取函数。 */
  getSearchText: (record: T) => string;
  /** 自定义过滤函数。 */
  filterDataSource?: (records: T[], keyword: string, getSearchText: (record: T) => string) => T[];
  /** 表格样式类名。 */
  tableClassName?: string;
  /** 表格横向滚动宽度。 */
  tableScrollX?: number;
  /** 新增回调。 */
  onCreate?: () => void;
}

function ModulePage<T extends { id: string | number }>({
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
  filterDataSource,
  tableClassName,
  tableScrollX,
  onCreate,
}: ModulePageProps<T>) {
  const [keyword, setKeyword] = useState('');

  const filteredData = filterDataSource
    ? filterDataSource(dataSource, keyword, getSearchText)
    : (
      keyword.trim()
        ? dataSource.filter((record) => getSearchText(record).toLowerCase().includes(keyword.trim().toLowerCase()))
        : dataSource
    );

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
            {onCreate ? (
              <Button type="primary" onClick={onCreate}>
                新增
              </Button>
            ) : null}
            <Button>批量导出</Button>
            <Tag color="blue">当前 {filteredData.length} 条</Tag>
          </Space>
        </div>
        <Table<T>
          rowKey="id"
          className={tableClassName}
          columns={columns}
          dataSource={filteredData}
          loading={loading}
          pagination={false}
          scroll={tableScrollX ? { x: tableScrollX } : undefined}
        />
      </Card>
    </div>
  );
}

export default ModulePage;
