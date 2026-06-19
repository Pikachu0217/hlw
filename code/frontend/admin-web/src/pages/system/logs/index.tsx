import { SearchOutlined } from '@ant-design/icons';
import { Card, Col, Input, Row, Space, Table, Tabs, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { fetchSystemLoginLogs, fetchSystemOperatorLogs } from '@/api/modules';
import PageHero from '@/components/PageHero';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface SystemLogRecord {
  key: string;
  tenantId?: string;
  userName?: string;
  clientKey?: string;
  deviceType?: string;
  ipaddr?: string;
  loginLocation?: string;
  browser?: string;
  os?: string;
  msg?: string;
  loginTime?: string;
  title?: string;
  businessType?: number;
  method?: string;
  requestMethod?: string;
  operatorType?: number;
  operatorName?: string;
  deptName?: string;
  operatorUrl?: string;
  operatorIp?: string;
  errorMsg?: string;
  operatorTime?: string;
  costTime?: number;
  status?: number;
}

function SystemLogsPage() {
  const { records: loginLogs, loading: loginLoading } = useModuleRecords(fetchSystemLoginLogs, '系统登录日志');
  const { records: operatorLogs, loading: operatorLoading } = useModuleRecords(fetchSystemOperatorLogs, '系统操作日志');
  const [loginKeyword, setLoginKeyword] = useState('');
  const [operatorKeyword, setOperatorKeyword] = useState('');

  const filteredLoginLogs = useMemo(
    () =>
      loginKeyword.trim()
        ? loginLogs.filter((record) =>
            `${record.userName ?? ''} ${record.ipaddr ?? ''} ${record.loginLocation ?? ''} ${record.browser ?? ''} ${record.os ?? ''}`
              .toLowerCase()
              .includes(loginKeyword.trim().toLowerCase()),
          )
        : loginLogs,
    [loginKeyword, loginLogs],
  );

  const filteredOperatorLogs = useMemo(
    () =>
      operatorKeyword.trim()
        ? operatorLogs.filter((record) =>
            `${record.title ?? ''} ${record.operatorName ?? ''} ${record.operatorUrl ?? ''} ${record.operatorIp ?? ''} ${record.method ?? ''}`
              .toLowerCase()
              .includes(operatorKeyword.trim().toLowerCase()),
          )
        : operatorLogs,
    [operatorKeyword, operatorLogs],
  );

  const loginColumns = useMemo<ColumnsType<SystemLogRecord>>(
    () => [
      { title: '账号', dataIndex: 'userName' },
      { title: '设备', dataIndex: 'deviceType' },
      { title: 'IP地址', dataIndex: 'ipaddr' },
      { title: '地点', dataIndex: 'loginLocation' },
      { title: '浏览器', dataIndex: 'browser' },
      { title: '系统', dataIndex: 'os' },
      {
        title: '状态',
        dataIndex: 'status',
        render: (value: number | undefined) => <Tag color={value === 0 ? 'green' : 'red'}>{value === 0 ? '成功' : '失败'}</Tag>,
      },
      { title: '提示', dataIndex: 'msg' },
      { title: '访问时间', dataIndex: 'loginTime' },
    ],
    [],
  );

  const operatorColumns = useMemo<ColumnsType<SystemLogRecord>>(
    () => [
      { title: '模块', dataIndex: 'title' },
      { title: '操作人员', dataIndex: 'operatorName' },
      { title: '请求方式', dataIndex: 'requestMethod' },
      { title: '请求URL', dataIndex: 'operatorUrl' },
      { title: '主机地址', dataIndex: 'operatorIp' },
      {
        title: '状态',
        dataIndex: 'status',
        render: (value: number | undefined) => <Tag color={value === 0 ? 'green' : 'red'}>{value === 0 ? '正常' : '异常'}</Tag>,
      },
      { title: '耗时', dataIndex: 'costTime', render: (value: number | undefined) => (value ? `${value} ms` : '-') },
      { title: '操作时间', dataIndex: 'operatorTime' },
    ],
    [],
  );

  return (
    <div className="page-shell">
      <PageHero eyebrow="系统管理" title="系统日志" description="查看后台登录日志和操作日志，辅助排查账号访问与接口调用问题。" />
      <Row gutter={[18, 18]}>
        <Col xs={24} md={8}>
          <Card className="metric-card" bordered={false}>
            <span className="metric-card__label">登录日志</span>
            <strong className="metric-card__value">{loginLogs.length}</strong>
            <Typography.Text className="metric-card__hint">来自系统登录日志接口</Typography.Text>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="metric-card" bordered={false}>
            <span className="metric-card__label">操作日志</span>
            <strong className="metric-card__value">{operatorLogs.length}</strong>
            <Typography.Text className="metric-card__hint">来自系统操作日志接口</Typography.Text>
          </Card>
        </Col>
        <Col xs={24} md={8}>
          <Card className="metric-card" bordered={false}>
            <span className="metric-card__label">异常记录</span>
            <strong className="metric-card__value">
              {loginLogs.filter((record) => record.status !== 0).length + operatorLogs.filter((record) => record.status !== 0).length}
            </strong>
            <Typography.Text className="metric-card__hint">按状态字段实时统计</Typography.Text>
          </Card>
        </Col>
      </Row>
      <Card className="console-card" bordered={false}>
        <Tabs
          items={[
            {
              key: 'login',
              label: '登录日志',
              children: (
                <>
                  <div className="console-card__toolbar">
                    <div>
                      <Typography.Title level={4} className="console-card__title">
                        登录日志列表
                      </Typography.Title>
                      <Typography.Text className="console-card__subtitle">记录后台账号登录设备、地点和结果。</Typography.Text>
                    </div>
                    <Space wrap>
                      <Input
                        allowClear
                        value={loginKeyword}
                        onChange={(event) => setLoginKeyword(event.target.value)}
                        prefix={<SearchOutlined />}
                        placeholder="搜索账号、IP、地点或设备"
                        className="console-card__search"
                      />
                      <Tag color="blue">当前 {filteredLoginLogs.length} 条</Tag>
                    </Space>
                  </div>
                  <Table<SystemLogRecord> rowKey="key" columns={loginColumns} dataSource={filteredLoginLogs} loading={loginLoading} pagination={false} />
                </>
              ),
            },
            {
              key: 'operator',
              label: '操作日志',
              children: (
                <>
                  <div className="console-card__toolbar">
                    <div>
                      <Typography.Title level={4} className="console-card__title">
                        操作日志列表
                      </Typography.Title>
                      <Typography.Text className="console-card__subtitle">记录后台接口调用、操作人员和执行耗时。</Typography.Text>
                    </div>
                    <Space wrap>
                      <Input
                        allowClear
                        value={operatorKeyword}
                        onChange={(event) => setOperatorKeyword(event.target.value)}
                        prefix={<SearchOutlined />}
                        placeholder="搜索模块、人员、URL 或 IP"
                        className="console-card__search"
                      />
                      <Tag color="blue">当前 {filteredOperatorLogs.length} 条</Tag>
                    </Space>
                  </div>
                  <Table<SystemLogRecord>
                    rowKey="key"
                    columns={operatorColumns}
                    dataSource={filteredOperatorLogs}
                    loading={operatorLoading}
                    pagination={false}
                  />
                </>
              ),
            },
          ]}
        />
      </Card>
    </div>
  );
}

export default SystemLogsPage;
