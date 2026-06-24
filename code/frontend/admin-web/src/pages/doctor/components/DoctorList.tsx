import { SearchOutlined } from '@ant-design/icons';
import { Button, Card, Input, Space, Table, Tag, Typography } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';

export interface DoctorRecord {
  id: string;
  doctorId?: number;
  userId?: string;
  name: string;
  title: string;
  department: string;
  specialty: string;
  status: string;
  consultStatus: string;
  schedule: string;
  patientCount: number;
  consultFee?: string;
  configured?: boolean;
}

interface DoctorListProps {
  doctors: DoctorRecord[];
  onEditDoctor: (doctor: DoctorRecord) => void;
  onCreateSchedule: (doctor: DoctorRecord) => void;
  onToggleStatus: (doctor: DoctorRecord) => void;
}

function DoctorList({ doctors, onEditDoctor, onCreateSchedule, onToggleStatus }: DoctorListProps) {
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
    { title: '问诊费用', dataIndex: 'consultFee', render: (value?: string) => (value ? `￥${value}` : '￥0') },
    { title: '今日排班', dataIndex: 'schedule' },
    {
      title: '接诊状态',
      dataIndex: 'status',
      render: (value: string, record: DoctorRecord) => (
        <Tag color={record.consultStatus === 'OFFLINE' ? 'default' : record.consultStatus === 'BUSY' ? 'gold' : 'blue'}>
          {value}
        </Tag>
      ),
    },
    { title: '今日患者', dataIndex: 'patientCount' },
    {
      title: '操作',
      key: 'actions',
      render: (_, record) => (
        <Space wrap>
          <Button size="small" type="link" onClick={() => onEditDoctor(record)}>
            编辑
          </Button>
          <Button size="small" onClick={() => onToggleStatus(record)}>
            {record.consultStatus === 'OFFLINE' ? '恢复接诊' : '停诊'}
          </Button>
          <Button size="small" onClick={() => onCreateSchedule(record)} disabled={!record.doctorId}>
            排班
          </Button>
        </Space>
      ),
    },
  ];

  const filteredDoctors = keyword.trim()
    ? doctors.filter((doctor) =>
        `${doctor.name} ${doctor.department} ${doctor.specialty} ${doctor.status} ${doctor.consultStatus}`
          .toLowerCase()
          .includes(keyword.trim().toLowerCase()),
      )
    : doctors;

  return (
    <Card className="console-card" bordered={false}>
      <div className="console-card__toolbar">
        <div>
          <Typography.Title level={4} className="console-card__title">
            医生资源列表
          </Typography.Title>
          <Typography.Text className="console-card__subtitle">维护已纳入互联网医院的医生线上资源。</Typography.Text>
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
          <Tag color="cyan">筛选后 {filteredDoctors.length} 位</Tag>
        </Space>
      </div>
      <Table<DoctorRecord> rowKey="id" columns={columns} dataSource={filteredDoctors} pagination={false} />
    </Card>
  );
}

export default DoctorList;
