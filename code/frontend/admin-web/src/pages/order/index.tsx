import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ModulePage from '@/components/ModulePage';

interface OrderRecord {
  key: string;
  orderNo: string;
  businessType: string;
  patientName: string;
  amount: string;
  payStatus: string;
  createdAt: string;
}

const dataSource: OrderRecord[] = [
  { key: '1', orderNo: 'DD20260612001', businessType: '门诊预约', patientName: '赵晓岚', amount: '¥58.00', payStatus: '已支付', createdAt: '09:12' },
  { key: '2', orderNo: 'DD20260612002', businessType: '图文咨询', patientName: '沈博远', amount: '¥39.90', payStatus: '待支付', createdAt: '09:35' },
];

const columns: ColumnsType<OrderRecord> = [
  { title: '订单号', dataIndex: 'orderNo' },
  { title: '业务类型', dataIndex: 'businessType' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '金额', dataIndex: 'amount' },
  { title: '支付状态', dataIndex: 'payStatus', render: (value: string) => <Tag color="blue">{value}</Tag> },
  { title: '创建时间', dataIndex: 'createdAt' },
];

function OrderPage() {
  return (
    <ModulePage<OrderRecord>
      eyebrow="订单中心"
      title="诊疗订单与支付状态"
      description="围绕业务类型、支付状态和金额做统一管理。"
      metrics={[
        { label: '今日订单', value: '214', hint: '处方购药增长明显' },
        { label: '待支付', value: '17', hint: '建议发起二次提醒' },
        { label: '退款处理中', value: '6', hint: '需财务复核' },
      ]}
      columns={columns}
      dataSource={dataSource}
      tableTitle="订单列表"
      searchPlaceholder="搜索订单号、患者或业务类型"
      getSearchText={(record) => `${record.orderNo} ${record.patientName} ${record.businessType} ${record.payStatus}`}
    />
  );
}

export default OrderPage;
