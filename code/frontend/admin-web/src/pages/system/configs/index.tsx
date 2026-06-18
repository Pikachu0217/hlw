import { Button, Form, Input, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createConfig, deleteConfig, fetchConfigs, updateConfig } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface ConfigRecord {
  key: string;
  configKey: string;
  configValue: string;
  configType: string;
  status: string;
  remark: string;
}

function ConfigsPage() {
  const { records, loading, refresh } = useModuleRecords(fetchConfigs, '参数配置');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<ConfigRecord | null>(null);

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ configType: '业务参数', status: '0' });
    setOpen(true);
  };

  const handleOpenEdit = (record: ConfigRecord) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateConfig(editingRecord.key, { configValue: values.configValue, remark: values.remark });
        message.success('参数配置更新成功');
      } else {
        await createConfig(values);
        message.success('参数配置创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning(editingRecord ? '参数配置更新失败，请检查接口或稍后重试' : '参数配置创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: ConfigRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除配置"${record.configKey}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteConfig(record.key);
          message.success('参数配置删除成功');
          refresh();
        } catch {
          message.warning('参数配置删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<ConfigRecord>>(
    () => [
      { title: '配置键', dataIndex: 'configKey' },
      { title: '配置值', dataIndex: 'configValue' },
      { title: '配置类型', dataIndex: 'configType' },
      { title: '备注', dataIndex: 'remark' },
      { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '启用' : '禁用'}</Tag> },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: ConfigRecord) => (
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
      <ModulePage<ConfigRecord>
        eyebrow="系统管理"
        title="参数配置"
        description="集中沉淀问诊时长、放号窗口、安全策略等可运营参数。"
        metrics={[
          { label: '配置项', value: String(records.length), hint: '来自后端配置接口' },
          { label: '配置类型', value: String(new Set(records.map((record) => record.configType)).size), hint: '按配置类型聚合' },
          { label: '启用配置', value: String(records.filter((record) => record.status === '0').length), hint: '按状态实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="参数配置列表"
        searchPlaceholder="搜索配置键、类型或备注"
        getSearchText={(record) => `${record.configKey} ${record.configType} ${record.configValue} ${record.remark}`}
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑参数配置' : '新增参数配置'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="configKey" label="配置键" rules={[{ required: !editingRecord, message: '请输入配置键' }]}>
            <Input placeholder="请输入配置键" disabled={Boolean(editingRecord)} />
          </Form.Item>
          <Form.Item name="configValue" label="配置值" rules={[{ required: true, message: '请输入配置值' }]}>
            <Input placeholder="请输入配置值" />
          </Form.Item>
          <Form.Item name="configType" label="配置类型">
            <Input placeholder="例如：业务参数" disabled={Boolean(editingRecord)} />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              disabled={Boolean(editingRecord)}
              options={[
                { label: '启用', value: '0' },
                { label: '禁用', value: '1' },
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

export default ConfigsPage;
