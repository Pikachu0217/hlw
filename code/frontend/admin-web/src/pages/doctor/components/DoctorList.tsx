import { SearchOutlined } from '@ant-design/icons';
import { Button, Card, Input, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';

export interface DoctorRecord {
  key: string;
  name: string;
  title: string;
  department: string;
  specialty: string;
  status: string;
  schedule: string;
  patientCount: number;
}

interface DoctorListProps {
  doctors: DoctorRecord[];
}

const columns: ColumnsType<DoctorRecord> = [
  {
    title: '医生姓名',
    dataIndex: 'name',
    render: (value: string, record: DoctorRecord) => (
      <div className="doctor-name-cell">
        <strong>{value}</strong>
        <span>{record.title}</span>
      </div>
    ),
  },
  { title: '所属科室', dataIndex: 'department' },
  { title: '擅长方向', dataIndex: 'specialty' },
  { title: '今日排班', dataIndex: 'schedule' },
  {
    title: '接诊状态',
    dataIndex: 'status',
    render: (value: string) => <Tag color={value === '接诊中' ? 'green' : value === '候诊' ? 'blue' : 'orange'}>{value}</Tag>,
  },
  { title: '今日患者', dataIndex: 'patientCount' },
];

// 为医生模块提供可复用的医生列表组件。
function DoctorList({ doctors }: DoctorListProps) {
  const [keyword, setKeyword] = useState('');

  // 根据搜索关键字筛选医生列表。
  function buildFilteredDoctors(): DoctorRecord[] {
    const normalizedKeyword = keyword.trim().toLowerCase();

    if (!normalizedKeyword) {
      return doctors;
    }

    return doctors.filter((doctor) =>
      `${doctor.name} ${doctor.department} ${doctor.specialty} ${doctor.status}`.toLowerCase().includes(normalizedKeyword),
    );
  }

  const filteredDoctors = buildFilteredDoctors();

  return (
    <Card className="console-card" bordered={false}>
      <div className="console-card__toolbar">
        <div>
          <Typography.Title level={4} className="console-card__title">
            医生列表
          </Typography.Title>
          <Typography.Text className="console-card__subtitle">
            组件已抽离，后续可直接在用例或页面中复用。
          </Typography.Text>
        </div>
        <Space wrap>
          <Input
            value={keyword}
            allowClear
            onChange={(event) => setKeyword(event.target.value)}
            prefix={<SearchOutlined />}
            placeholder="搜索医生、科室或擅长方向"
            className="console-card__search"
          />
          <Button type="primary">新增医生</Button>
          <Tag color="cyan">筛选后 {filteredDoctors.length} 位</Tag>
        </Space>
      </div>
      <Table<DoctorRecord> rowKey="key" columns={columns} dataSource={filteredDoctors} pagination={false} />
    </Card>
  );
}

export default DoctorList;
