import { Button, Form, Input, Modal, Space, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createConfig, deleteConfig, fetchConfigs, updateConfig } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface ConfigRecord {
  id: number;
  configName: string;
  configKey: string;
  configValue: string;
  remark?: string;
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
        await updateConfig(editingRecord.id, values);
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
          await deleteConfig(record.id);
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
      { title: '配置名称', dataIndex: 'configName' },
      { title: '配置键', dataIndex: 'configKey' },
      { title: '配置值', dataIndex: 'configValue' },
      { title: '备注', dataIndex: 'remark' },
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
          { label: '配置键', value: String(new Set(records.map((record) => record.configKey)).size), hint: '按配置键去重' },
          { label: '有备注项', value: String(records.filter((record) => record.remark).length), hint: '按备注字段实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="参数配置列表"
        searchPlaceholder="搜索配置名称、键或备注"
        getSearchText={(record) => `${record.configName} ${record.configKey} ${record.configValue} ${record.remark ?? ''}`}
        tableClassName="system-compact-table"
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
          <Form.Item name="configName" label="配置名称" rules={[{ required: true, message: '请输入配置名称' }]}>
            <Input placeholder="请输入配置名称" />
          </Form.Item>
          <Form.Item name="configKey" label="配置键" rules={[{ required: true, message: '请输入配置键' }]}>
            <Input placeholder="请输入配置键" />
          </Form.Item>
          <Form.Item name="configValue" label="配置值" rules={[{ required: true, message: '请输入配置值' }]}>
            <Input placeholder="请输入配置值" />
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
