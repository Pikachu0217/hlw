import { Form, Input, InputNumber, Modal, Select, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createDict, fetchDicts } from '@/api/modules';
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

const columns: ColumnsType<DictRecord> = [
  { title: '字典类型', dataIndex: 'dictType' },
  { title: '字典标签', dataIndex: 'dictLabel' },
  { title: '字典键值', dataIndex: 'dictValue' },
  { title: '排序', dataIndex: 'sort' },
  { title: '备注', dataIndex: 'remark' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function DictsPage() {
  const { records, loading, refresh } = useModuleRecords(fetchDicts, '字典');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const dictTypeCount = new Set(records.map((record) => record.dictType)).size;

  const handleCreate = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await createDict(values);
      message.success('字典项创建成功');
      setOpen(false);
      form.resetFields();
      refresh();
    } catch {
      message.warning('字典项创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <ModulePage<DictRecord>
        eyebrow="系统管理"
        title="字典管理"
        description="统一维护账号状态、菜单类型和业务枚举，减少页面与服务中的硬编码。"
        metrics={[
          { label: '字典项', value: String(records.length), hint: '来自后端字典接口' },
          { label: '字典类型', value: String(dictTypeCount), hint: '按 dictType 聚合' },
          { label: '启用项', value: String(records.filter((record) => record.status === '启用').length), hint: '按状态实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="字典项列表"
        searchPlaceholder="搜索类型、标签或键值"
        getSearchText={(record) => `${record.dictType} ${record.dictLabel} ${record.dictValue} ${record.remark}`}
        onCreate={() => setOpen(true)}
      />
      <Modal
        title="新增字典项"
        open={open}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form" initialValues={{ sort: 0, status: '启用' }}>
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
                { label: '启用', value: '启用' },
                { label: '停用', value: '停用' },
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
