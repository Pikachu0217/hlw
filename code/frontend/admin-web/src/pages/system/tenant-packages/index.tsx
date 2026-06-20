import { Button, Form, Input, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createTenantPackage, deleteTenantPackage, fetchTenantPackages, updateTenantPackage } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface TenantPackageRecord {
  id: number;
  packageName: string;
  remark?: string;
  status: number;
  menuIds?: number[];
}

interface TenantPackageFormValues {
  packageName: string;
  remark?: string;
  status?: number;
  menuIdsText?: string;
}

function parseMenuIds(menuIdsText?: string): number[] {
  if (!menuIdsText) {
    return [];
  }
  return menuIdsText
    .split(',')
    .map((item) => Number(item.trim()))
    .filter((item) => Number.isFinite(item) && item > 0);
}

function TenantPackagesPage() {
  const { records, loading, refresh } = useModuleRecords(fetchTenantPackages, '租户套餐');
  const [form] = Form.useForm<TenantPackageFormValues>();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<TenantPackageRecord | null>(null);

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ status: 0 });
    setOpen(true);
  };

  const handleOpenEdit = (record: TenantPackageRecord) => {
    setEditingRecord(record);
    form.setFieldsValue({
      packageName: record.packageName,
      remark: record.remark,
      status: record.status,
      menuIdsText: record.menuIds?.join(','),
    });
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    const payload = {
      packageName: values.packageName,
      remark: values.remark,
      status: values.status,
      menuIds: parseMenuIds(values.menuIdsText),
    };
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateTenantPackage(editingRecord.id, payload);
        message.success('租户套餐更新成功');
      } else {
        await createTenantPackage(payload);
        message.success('租户套餐创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning(editingRecord ? '租户套餐更新失败，请检查接口或稍后重试' : '租户套餐创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: TenantPackageRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除租户套餐"${record.packageName}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteTenantPackage(record.id);
          message.success('租户套餐删除成功');
          refresh();
        } catch {
          message.warning('租户套餐删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<TenantPackageRecord>>(
    () => [
      { title: '套餐名称', dataIndex: 'packageName' },
      { title: '菜单数量', dataIndex: 'menuIds', render: (value: number[] | undefined) => value?.length ?? 0 },
      { title: '备注', dataIndex: 'remark' },
      {
        title: '状态',
        dataIndex: 'status',
        render: (value: number) => <Tag color={value === 0 ? 'green' : 'default'}>{value === 0 ? '启用' : '禁用'}</Tag>,
      },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: TenantPackageRecord) => (
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
      <ModulePage<TenantPackageRecord>
        eyebrow="系统管理"
        title="租户套餐管理"
        description="维护租户可用菜单集合与套餐启停状态，支撑租户授权配置。"
        metrics={[
          { label: '套餐数', value: String(records.length), hint: '来自后端租户套餐接口' },
          { label: '启用套餐', value: String(records.filter((record) => record.status === 0).length), hint: '按状态实时统计' },
          { label: '菜单授权', value: String(records.reduce((sum, record) => sum + (record.menuIds?.length ?? 0), 0)), hint: '汇总套餐菜单数量' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="租户套餐列表"
        searchPlaceholder="搜索套餐名称或备注"
        getSearchText={(record) => `${record.packageName} ${record.remark ?? ''}`}
        tableClassName="system-compact-table"
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑租户套餐' : '新增租户套餐'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="packageName" label="套餐名称" rules={[{ required: true, message: '请输入套餐名称' }]}>
            <Input placeholder="请输入套餐名称" />
          </Form.Item>
          <Form.Item name="menuIdsText" label="菜单编号">
            <Input placeholder="例如：1,2,3" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={[
                { label: '启用', value: 0 },
                { label: '禁用', value: 1 },
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

export default TenantPackagesPage;
