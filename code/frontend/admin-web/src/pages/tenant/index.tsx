import { Button, Form, Input, InputNumber, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createTenant, deleteTenant, fetchTenants, updateTenant } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface TenantRecord {
  id: number;
  tenantId: string;
  contactUserName: string;
  contactPhone: string;
  companyName: string;
  licenseNumber?: string;
  address?: string;
  intro?: string;
  domain?: string;
  packageId?: number;
  packageName?: string;
  expireTime?: string;
  accountCount?: number;
  status: string;
  remark?: string;
}

function TenantPage() {
  const { records, loading, refresh } = useModuleRecords(fetchTenants, '租户');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<TenantRecord | null>(null);
  const disabledCount = records.filter((record) => record.status !== '0').length;

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ packageId: 1, accountCount: 50, status: '0' });
    setOpen(true);
  };

  const handleOpenEdit = (record: TenantRecord) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateTenant(editingRecord.id, values);
        message.success('租户更新成功');
      } else {
        await createTenant(values);
        message.success('租户创建成功');
      }
      setOpen(false);
      form.resetFields();
      setEditingRecord(null);
      refresh();
    } catch {
      message.warning(editingRecord ? '租户更新失败，请检查接口或稍后重试' : '租户创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: TenantRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除租户"${record.companyName}"吗？此操作不可恢复。`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteTenant(record.id);
          message.success('租户删除成功');
          refresh();
        } catch {
          message.warning('租户删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<TenantRecord>>(
    () => [
      { title: '企业名称', dataIndex: 'companyName' },
      { title: '联系人', dataIndex: 'contactUserName' },
      { title: '联系电话', dataIndex: 'contactPhone' },
      { title: '套餐', dataIndex: 'packageName', render: (value: string | undefined) => value ?? '默认套餐' },
      { title: '账号额度', dataIndex: 'accountCount' },
      { title: '到期时间', dataIndex: 'expireTime' },
      {
        title: '状态',
        dataIndex: 'status',
        render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '正常' : '停用'}</Tag>,
      },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: TenantRecord) => (
          <Space size="small">
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
      <ModulePage<TenantRecord>
        eyebrow="租户中心"
        title="多租户运营总览"
        description="围绕企业主体、联系人、套餐额度与到期时间管理租户基础资料。"
        badgeText={`${records.length} 个租户`}
        metrics={[
          { label: '正常租户', value: String(records.length - disabledCount), hint: '来自后端租户接口' },
          { label: '停用租户', value: String(disabledCount), hint: '按租户状态实时统计' },
          { label: '账号额度', value: String(records.reduce((sum, record) => sum + (record.accountCount ?? 0), 0)), hint: '汇总当前账号上限' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="租户列表"
        searchPlaceholder="搜索企业、联系人或套餐"
        getSearchText={(record) =>
          `${record.companyName} ${record.contactUserName} ${record.contactPhone} ${record.packageName ?? ''} ${record.status}`
        }
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑租户' : '新增租户'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="companyName" label="企业名称" rules={[{ required: true, message: '请输入企业名称' }]}>
            <Input placeholder="请输入企业名称" />
          </Form.Item>
          <Form.Item name="contactUserName" label="联系人" rules={[{ required: true, message: '请输入联系人' }]}>
            <Input placeholder="请输入联系人" />
          </Form.Item>
          <Form.Item name="contactPhone" label="联系电话" rules={[{ required: true, message: '请输入联系电话' }]}>
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item name="packageId" label="套餐编号">
            <InputNumber min={1} className="module-form__number" />
          </Form.Item>
          <Form.Item name="expireTime" label="到期时间">
            <Input placeholder="例如：2026-12-31 23:59:59" />
          </Form.Item>
          <Form.Item name="accountCount" label="账号额度">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="licenseNumber" label="统一社会信用代码">
            <Input placeholder="请输入统一社会信用代码" />
          </Form.Item>
          <Form.Item name="address" label="企业地址">
            <Input placeholder="请输入企业地址" />
          </Form.Item>
          <Form.Item name="domain" label="访问域名">
            <Input placeholder="请输入访问域名" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={[
                { label: '正常', value: '0' },
                { label: '停用', value: '1' },
              ]}
            />
          </Form.Item>
          <Form.Item name="remark" label="备注">
            <Input.TextArea rows={3} placeholder="请输入备注" />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default TenantPage;
