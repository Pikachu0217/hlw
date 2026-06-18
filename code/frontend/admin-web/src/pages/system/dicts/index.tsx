import { Button, Form, Input, InputNumber, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createDict, deleteDict, fetchDicts, updateDict } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface DictRecord {
  key: string;
  dictType: string;
  dictLabel: string;
  dictValue: string;
  sort: number;
  status: string;
  remark: string;
}

function DictsPage() {
  const { records, loading, refresh } = useModuleRecords(fetchDicts, '字典');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<DictRecord | null>(null);
  const dictTypeCount = new Set(records.map((record) => record.dictType)).size;

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ sort: 0, status: '0' });
    setOpen(true);
  };

  const handleOpenEdit = (record: DictRecord) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateDict(editingRecord.key, values);
        message.success('字典项更新成功');
      } else {
        await createDict(values);
        message.success('字典项创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning(editingRecord ? '字典项更新失败，请检查接口或稍后重试' : '字典项创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: DictRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除字典项"${record.dictLabel}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteDict(record.key);
          message.success('字典项删除成功');
          refresh();
        } catch {
          message.warning('字典项删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<DictRecord>>(
    () => [
      { title: '字典类型', dataIndex: 'dictType' },
      { title: '字典标签', dataIndex: 'dictLabel' },
      { title: '字典键值', dataIndex: 'dictValue' },
      { title: '排序', dataIndex: 'sort' },
      { title: '备注', dataIndex: 'remark' },
      { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '启用' : '禁用'}</Tag> },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: DictRecord) => (
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
      <ModulePage<DictRecord>
        eyebrow="系统管理"
        title="字典管理"
        description="统一维护账号状态、菜单类型和业务枚举，减少页面与服务中的硬编码。"
        metrics={[
          { label: '字典项', value: String(records.length), hint: '来自后端字典接口' },
          { label: '字典类型', value: String(dictTypeCount), hint: '按 dictType 聚合' },
          { label: '启用项', value: String(records.filter((record) => record.status === '0').length), hint: '按状态实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="字典项列表"
        searchPlaceholder="搜索类型、标签或键值"
        getSearchText={(record) => `${record.dictType} ${record.dictLabel} ${record.dictValue} ${record.remark}`}
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑字典项' : '新增字典项'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="dictType" label="字典类型" rules={[{ required: true, message: '请输入字典类型' }]}>
            <Input placeholder="例如：user_status" />
          </Form.Item>
          <Form.Item name="dictLabel" label="字典标签" rules={[{ required: true, message: '请输入字典标签' }]}>
            <Input placeholder="请输入字典标签" />
          </Form.Item>
          <Form.Item name="dictValue" label="字典键值" rules={[{ required: true, message: '请输入字典键值' }]}>
            <Input placeholder="请输入字典键值" />
          </Form.Item>
          <Form.Item name="sort" label="排序">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
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

export default DictsPage;
