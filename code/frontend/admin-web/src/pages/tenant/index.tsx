import { Form, Input, Modal, Select, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createTenant, fetchTenants } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface TenantRecord {
  key: string;
  tenantName: string;
  packageName: string;
  adminName: string;
  expireAt: string;
  status: string;
}

const columns: ColumnsType<TenantRecord> = [
  { title: '租户名称', dataIndex: 'tenantName' },
  { title: '套餐版本', dataIndex: 'packageName' },
  { title: '管理员', dataIndex: 'adminName' },
  { title: '到期时间', dataIndex: 'expireAt' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '正常' ? 'green' : 'blue'}>{value}</Tag> },
];

function TenantPage() {
  const { records, loading, refresh } = useModuleRecords(fetchTenants, '租户');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const warningCount = records.filter((record) => record.status !== '正常').length;

  const handleCreate = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await createTenant(values);
      message.success('租户创建成功');
      setOpen(false);
      form.resetFields();
      refresh();
    } catch {
      message.warning('租户创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <ModulePage<TenantRecord>
        eyebrow="租户中心"
        title="多租户运营总览"
        description="围绕套餐、管理员与到期时间搭建租户基础运营面板。"
        badgeText={`${records.length} 个租户`}
        metrics={[
          { label: '活跃租户', value: String(records.length - warningCount), hint: '来自后端租户接口' },
          { label: '续费跟进', value: String(warningCount), hint: '按租户状态实时统计' },
          { label: '租户总数', value: String(records.length), hint: '覆盖当前系统租户' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="租户列表"
        searchPlaceholder="搜索租户、管理员或套餐"
        getSearchText={(record) => `${record.tenantName} ${record.adminName} ${record.packageName} ${record.status}`}
        onCreate={() => setOpen(true)}
      />
      <Modal
        title="新增租户"
        open={open}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form" initialValues={{ packageName: '标准医疗版', status: '正常' }}>
          <Form.Item name="tenantName" label="租户名称" rules={[{ required: true, message: '请输入租户名称' }]}>
            <Input placeholder="请输入租户名称" />
          </Form.Item>
          <Form.Item name="packageName" label="套餐版本" rules={[{ required: true, message: '请输入套餐版本' }]}>
            <Input placeholder="例如：标准医疗版" />
          </Form.Item>
          <Form.Item name="adminName" label="管理员" rules={[{ required: true, message: '请输入管理员名称' }]}>
            <Input placeholder="请输入管理员名称" />
          </Form.Item>
          <Form.Item name="expireAt" label="到期日期" rules={[{ required: true, message: '请输入到期日期' }]}>
            <Input placeholder="例如：2026-12-31" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={[
                { label: '正常', value: '正常' },
                { label: '续费跟进', value: '续费跟进' },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default TenantPage;
