import { Button, Form, Input, InputNumber, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createOrder, fetchOrders, payOrder } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface OrderRecord {
  key: string;
  orderNo: string;
  businessType: string;
  patientName: string;
  amount: string;
  payStatus: string;
  createdAt: string;
}

const columns = (onPay: (record: OrderRecord) => void): ColumnsType<OrderRecord> => [
  { title: '订单号', dataIndex: 'orderNo' },
  { title: '业务类型', dataIndex: 'businessType' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '金额', dataIndex: 'amount' },
  { title: '支付状态', dataIndex: 'payStatus', render: (value: string) => <Tag color={statusColor(value)}>{value}</Tag> },
  { title: '创建时间', dataIndex: 'createdAt' },
  {
    title: '操作',
    key: 'actions',
    render: (_, record) => (
      <Space wrap>
        <Button type="link" onClick={() => onPay(record)} disabled={record.payStatus !== '待支付'}>
          支付
        </Button>
      </Space>
    ),
  },
];

function OrderPage() {
  const { records, loading, refresh } = useModuleRecords(fetchOrders, '订单');
  const [createForm] = Form.useForm();
  const [payForm] = Form.useForm();
  const [createOpen, setCreateOpen] = useState(false);
  const [payOpen, setPayOpen] = useState(false);
  const [currentRecord, setCurrentRecord] = useState<OrderRecord | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const pendingCount = records.filter((record) => record.payStatus.includes('待')).length;

  async function handleCreate() {
    const values = await createForm.validateFields();
    setSubmitting(true);
    try {
      await createOrder(values);
      message.success('订单创建成功');
      setCreateOpen(false);
      createForm.resetFields();
      refresh();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '订单创建失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  function openPayModal(record: OrderRecord) {
    setCurrentRecord(record);
    payForm.setFieldsValue({ payMethod: 'MOCK_PAY' });
    setPayOpen(true);
  }

  async function handlePay() {
    if (!currentRecord) {
      return;
    }
    const values = await payForm.validateFields();
    setSubmitting(true);
    try {
      await payOrder(currentRecord.key, values);
      message.success('订单支付成功');
      setPayOpen(false);
      setCurrentRecord(null);
      payForm.resetFields();
      refresh();
    } catch (error) {
      message.warning(error instanceof Error ? error.message : '订单支付失败，请稍后重试');
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <>
      <ModulePage<OrderRecord>
        eyebrow="订单中心"
        title="诊疗订单与支付状态"
        description="围绕业务类型、订单金额和支付状态做统一管理。"
        badgeText="订单工作流已接入数据库"
        metrics={[
          { label: '今日订单', value: String(records.length), hint: '来自后端订单接口' },
          { label: '待支付', value: String(pendingCount), hint: '按支付状态实时统计' },
          { label: '已支付', value: String(records.length - pendingCount), hint: '可继续对接履约流转' },
        ]}
        columns={columns(openPayModal)}
        dataSource={records}
        loading={loading || submitting}
        tableTitle="订单列表"
        searchPlaceholder="搜索订单号、患者或业务类型"
        getSearchText={(record) => `${record.orderNo} ${record.patientName} ${record.businessType} ${record.payStatus}`}
        onCreate={() => setCreateOpen(true)}
      />
      <Modal
        title="新增订单"
        open={createOpen}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setCreateOpen(false)}
        destroyOnClose
      >
        <Form
          form={createForm}
          layout="vertical"
          className="module-form"
          initialValues={{
            bizType: 'APPOINTMENT',
            patientName: '张小满',
            amount: 25,
          }}
        >
          <Form.Item name="bizType" label="业务类型">
            <Select
              options={[
                { label: '门诊预约', value: 'APPOINTMENT' },
                { label: '图文咨询', value: 'CONSULT' },
                { label: '处方购药', value: 'PRESCRIPTION' },
                { label: '药品配送', value: 'DRUG' },
              ]}
            />
          </Form.Item>
          <Form.Item name="patientName" label="患者姓名" rules={[{ required: true, message: '请输入患者姓名' }]}>
            <Input placeholder="请输入患者姓名" />
          </Form.Item>
          <Form.Item name="bizId" label="业务编号">
            <InputNumber min={0} className="module-form__number" />
          </Form.Item>
          <Form.Item name="amount" label="订单金额" rules={[{ required: true, message: '请输入订单金额' }]}>
            <InputNumber min={0} precision={2} className="module-form__number" />
          </Form.Item>
        </Form>
      </Modal>
      <Modal
        title="支付订单"
        open={payOpen}
        confirmLoading={submitting}
        onOk={handlePay}
        onCancel={() => {
          setPayOpen(false);
          setCurrentRecord(null);
        }}
        destroyOnClose
      >
        <Form form={payForm} layout="vertical" className="module-form">
          <Form.Item label="订单号">
            <Input value={currentRecord?.orderNo} disabled />
          </Form.Item>
          <Form.Item name="payMethod" label="支付方式">
            <Select
              options={[
                { label: '模拟支付', value: 'MOCK_PAY' },
                { label: '微信支付', value: 'WECHAT' },
                { label: '支付宝', value: 'ALIPAY' },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

function statusColor(status: string): string {
  switch (status) {
    case '待支付':
      return 'gold';
    case '已支付':
      return 'green';
    case '已关闭':
      return 'default';
    case '已退款':
      return 'red';
    default:
      return 'processing';
  }
}

export default OrderPage;
