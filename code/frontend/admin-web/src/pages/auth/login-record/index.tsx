import { Button, Descriptions, Form, Input, InputNumber, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createLoginRecord, deleteLoginRecord, fetchLoginRecordDetail, fetchLoginRecords, updateLoginRecord } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface LoginRecord {
  key: string;
  tenantId: number;
  userId?: number;
  username: string;
  userType: string;
  loginStatus: string;
  failureReason: string;
  tokenDigest: string;
  loginTime: string;
  logoutTime?: string;
  clientIp: string;
  userAgent: string;
}

function LoginRecordPage() {
  const { records, loading, refresh } = useModuleRecords(fetchLoginRecords, '登录记录');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<LoginRecord | null>(null);
  const [detailRecord, setDetailRecord] = useState<LoginRecord | null>(null);

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ tenantId: 100, userType: 'ADMIN', loginStatus: 'SUCCESS' });
    setOpen(true);
  };

  const handleOpenEdit = (record: LoginRecord) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setOpen(true);
  };

  const handleOpenDetail = async (record: LoginRecord) => {
    try {
      const detail = await fetchLoginRecordDetail(record.key);
      setDetailRecord(detail);
      setDetailOpen(true);
    } catch {
      message.warning('登录记录详情加载失败，请稍后重试');
    }
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateLoginRecord(editingRecord.key, {
          loginStatus: values.loginStatus,
          failureReason: values.failureReason,
          logoutTime: values.logoutTime,
          clientIp: values.clientIp,
          userAgent: values.userAgent,
        });
        message.success('登录记录更新成功');
      } else {
        await createLoginRecord(values);
        message.success('登录记录创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning(editingRecord ? '登录记录更新失败，请检查接口或稍后重试' : '登录记录创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: LoginRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除账号"${record.username}"的登录记录吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteLoginRecord(record.key);
          message.success('登录记录删除成功');
          refresh();
        } catch {
          message.warning('登录记录删除失败，请稍后重试');
        }
      },
    });
  };

  const statusColorMap: Record<string, string> = {
    SUCCESS: 'green',
    FAILED: 'red',
    LOGOUT: 'blue',
  };

  const columns = useMemo<ColumnsType<LoginRecord>>(
    () => [
      { title: '租户', dataIndex: 'tenantId' },
      { title: '账号', dataIndex: 'username' },
      { title: '用户类型', dataIndex: 'userType' },
      {
        title: '状态',
        dataIndex: 'loginStatus',
        render: (value: string) => <Tag color={statusColorMap[value] ?? 'default'}>{value}</Tag>,
      },
      { title: '客户端 IP', dataIndex: 'clientIp' },
      { title: '登录时间', dataIndex: 'loginTime' },
      { title: '退出时间', dataIndex: 'logoutTime' },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: LoginRecord) => (
          <Space size="small">
            <Button type="link" size="small" onClick={() => handleOpenDetail(record)}>
              详情
            </Button>
            <Button type="link" size="small" onClick={() => handleOpenEdit(record)}>
              编辑
            </Button>
            <Button type="link" size="small" danger onClick={() => handleDelete(record)}>
              删除
            </Button>
          </Space>
        ),
      },
    ],
    [],
  );

  return (
    <>
      <ModulePage<LoginRecord>
        eyebrow="认证中心"
        title="登录记录"
        description="按租户、账号、客户端和登录状态沉淀认证链路审计数据。"
        metrics={[
          { label: '记录总数', value: String(records.length), hint: '来自认证登录记录接口' },
          { label: '成功登录', value: String(records.filter((record) => record.loginStatus === 'SUCCESS').length), hint: '按登录状态统计' },
          { label: '失败登录', value: String(records.filter((record) => record.loginStatus === 'FAILED').length), hint: '辅助排查认证异常' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="登录记录列表"
        searchPlaceholder="搜索账号、IP、状态或客户端"
        getSearchText={(record) => `${record.username} ${record.clientIp} ${record.loginStatus} ${record.userAgent}`}
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑登录记录' : '新增登录记录'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="tenantId" label="租户编号" rules={[{ required: true, message: '请输入租户编号' }]}>
            <InputNumber min={0} className="module-form__number" disabled={Boolean(editingRecord)} />
          </Form.Item>
          <Form.Item name="userId" label="用户编号">
            <InputNumber min={1} className="module-form__number" disabled={Boolean(editingRecord)} />
          </Form.Item>
          <Form.Item name="username" label="登录账号" rules={[{ required: !editingRecord, message: '请输入登录账号' }]}>
            <Input placeholder="请输入登录账号" disabled={Boolean(editingRecord)} />
          </Form.Item>
          <Form.Item name="userType" label="用户类型">
            <Select
              disabled={Boolean(editingRecord)}
              options={[
                { label: '管理员', value: 'ADMIN' },
                { label: '医生', value: 'DOCTOR' },
                { label: '药师', value: 'PHARMACIST' },
                { label: '患者', value: 'PATIENT' },
              ]}
            />
          </Form.Item>
          <Form.Item name="loginStatus" label="登录状态" rules={[{ required: true, message: '请选择登录状态' }]}>
            <Select
              options={[
                { label: '成功', value: 'SUCCESS' },
                { label: '失败', value: 'FAILED' },
                { label: '已退出', value: 'LOGOUT' },
              ]}
            />
          </Form.Item>
          <Form.Item name="failureReason" label="失败原因">
            <Input placeholder="请输入失败原因" />
          </Form.Item>
          <Form.Item name="tokenDigest" label="令牌摘要">
            <Input placeholder="请输入令牌摘要" disabled={Boolean(editingRecord)} />
          </Form.Item>
          <Form.Item name="logoutTime" label="退出时间">
            <Input placeholder="yyyy-MM-dd HH:mm:ss" />
          </Form.Item>
          <Form.Item name="clientIp" label="客户端 IP">
            <Input placeholder="请输入客户端 IP" />
          </Form.Item>
          <Form.Item name="userAgent" label="客户端标识">
            <Input.TextArea rows={3} placeholder="请输入客户端标识" />
          </Form.Item>
        </Form>
      </Modal>
      <Modal title="登录记录详情" open={detailOpen} onCancel={() => setDetailOpen(false)} footer={null} destroyOnClose>
        {detailRecord ? (
          <Descriptions column={1} size="small" bordered>
            <Descriptions.Item label="租户编号">{detailRecord.tenantId}</Descriptions.Item>
            <Descriptions.Item label="用户编号">{detailRecord.userId ?? '-'}</Descriptions.Item>
            <Descriptions.Item label="登录账号">{detailRecord.username}</Descriptions.Item>
            <Descriptions.Item label="用户类型">{detailRecord.userType}</Descriptions.Item>
            <Descriptions.Item label="登录状态">{detailRecord.loginStatus}</Descriptions.Item>
            <Descriptions.Item label="失败原因">{detailRecord.failureReason || '-'}</Descriptions.Item>
            <Descriptions.Item label="令牌摘要">{detailRecord.tokenDigest || '-'}</Descriptions.Item>
            <Descriptions.Item label="登录时间">{detailRecord.loginTime}</Descriptions.Item>
            <Descriptions.Item label="退出时间">{detailRecord.logoutTime || '-'}</Descriptions.Item>
            <Descriptions.Item label="客户端 IP">{detailRecord.clientIp || '-'}</Descriptions.Item>
            <Descriptions.Item label="客户端标识">{detailRecord.userAgent || '-'}</Descriptions.Item>
          </Descriptions>
        ) : null}
      </Modal>
    </>
  );
}

export default LoginRecordPage;
