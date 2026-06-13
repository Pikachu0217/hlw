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
  onCreateDoctor: () => void;
  onCreateSchedule: (doctor: DoctorRecord) => void;
  onToggleStatus: (doctor: DoctorRecord) => void;
}

function DoctorList({ doctors, onCreateDoctor, onCreateSchedule, onToggleStatus }: DoctorListProps) {
  const [keyword, setKeyword] = useState('');
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
    { title: '接诊状态', dataIndex: 'status', render: (value: string) => <Tag color={value.includes('停') ? 'default' : 'blue'}>{value}</Tag> },
    { title: '今日患者', dataIndex: 'patientCount' },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Space wrap>
          <Button size="small" onClick={() => onToggleStatus(record)}>
            {record.status.includes('停') ? '恢复接诊' : '停诊'}
          </Button>
          <Button size="small" onClick={() => onCreateSchedule(record)}>
            排班
          </Button>
        </Space>
      ),
    },
  ];

  const filteredDoctors = keyword.trim()
    ? doctors.filter((doctor) =>
        `${doctor.name} ${doctor.department} ${doctor.specialty} ${doctor.status}`
          .toLowerCase()
          .includes(keyword.trim().toLowerCase()),
      )
    : doctors;

  return (
    <Card className="console-card" bordered={false}>
      <div className="console-card__toolbar">
        <div>
          <Typography.Title level={4} className="console-card__title">
            医生列表
          </Typography.Title>
          <Typography.Text className="console-card__subtitle">组件已抽离，后续可直接复用。</Typography.Text>
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
          <Button type="primary" onClick={onCreateDoctor}>新增医生</Button>
          <Tag color="cyan">筛选后 {filteredDoctors.length} 位</Tag>
        </Space>
      </div>
      <Table<DoctorRecord> rowKey="key" columns={columns} dataSource={filteredDoctors} pagination={false} />
    </Card>
  );
}

export default DoctorList;
