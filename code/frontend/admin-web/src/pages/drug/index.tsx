import { Form, Input, InputNumber, Modal, Select, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createDrug, fetchDrugs } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface DrugRecord {
  key: string;
  drugName: string;
  spec: string;
  inventory: number;
  unit: string;
  warningStatus: string;
}

const columns: ColumnsType<DrugRecord> = [
  { title: '药品名称', dataIndex: 'drugName' },
  { title: '规格', dataIndex: 'spec' },
  { title: '库存', dataIndex: 'inventory' },
  { title: '单位', dataIndex: 'unit' },
  { title: '预警状态', dataIndex: 'warningStatus', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function DrugPage() {
  const { records, loading, refresh } = useModuleRecords(fetchDrugs, '药品');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const warningCount = records.filter((record) => record.warningStatus.includes('预警')).length;

  const handleCreate = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await createDrug(values);
      message.success('药品创建成功');
      setOpen(false);
      form.resetFields();
      refresh();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '药品创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <ModulePage<DrugRecord>
        eyebrow="药品目录"
        title="药品库存与预警"
        description="先沉淀药品基础目录、规格和库存预警状态。"
        metrics={[
          { label: '在售药品', value: String(records.length), hint: '来自后端药品接口' },
          { label: '库存预警', value: String(warningCount), hint: '按预警状态实时统计' },
          { label: '库存合计', value: String(records.reduce((sum, item) => sum + item.inventory, 0)), hint: '汇总当前列表库存' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="药品列表"
        searchPlaceholder="搜索药品名称、规格或状态"
        getSearchText={(record) => `${record.drugName} ${record.spec} ${record.warningStatus}`}
        onCreate={() => setOpen(true)}
      />
      <Modal
        title="新增药品"
        open={open}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form" initialValues={{ inventory: 100, unit: '盒' }}>
          <Form.Item name="drugName" label="药品名称" rules={[{ required: true, message: '请输入药品名称' }]}>
            <Input placeholder="请输入药品名称" />
          </Form.Item>
          <Form.Item name="spec" label="规格" rules={[{ required: true, message: '请输入药品规格' }]}>
            <Input placeholder="例如：10mg*12片" />
          </Form.Item>
          <Form.Item name="inventory" label="初始库存">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="unit" label="单位">
            <Select
              options={[
                { label: '盒', value: '盒' },
                { label: '瓶', value: '瓶' },
                { label: '袋', value: '袋' },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default DrugPage;
