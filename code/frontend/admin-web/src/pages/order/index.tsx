import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchOrders } from '@/api/modules';
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

const columns: ColumnsType<OrderRecord> = [
  { title: '订单号', dataIndex: 'orderNo' },
  { title: '业务类型', dataIndex: 'businessType' },
  { title: '患者', dataIndex: 'patientName' },
  { title: '金额', dataIndex: 'amount' },
  { title: '支付状态', dataIndex: 'payStatus', render: (value: string) => <Tag color="blue">{value}</Tag> },
  { title: '创建时间', dataIndex: 'createdAt' },
];

function OrderPage() {
  const { records, loading } = useModuleRecords(fetchOrders, '订单');
  const pendingCount = records.filter((record) => record.payStatus.includes('待')).length;

  return (
    <ModulePage<OrderRecord>
      eyebrow="订单中心"
      title="诊疗订单与支付状态"
      description="围绕业务类型、支付状态和金额做统一管理。"
      metrics={[
        { label: '今日订单', value: String(records.length), hint: '来自后端订单接口' },
        { label: '待支付', value: String(pendingCount), hint: '按支付状态实时统计' },
        { label: '已支付', value: String(records.length - pendingCount), hint: '可继续对接履约流转' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="订单列表"
      searchPlaceholder="搜索订单号、患者或业务类型"
      getSearchText={(record) => `${record.orderNo} ${record.patientName} ${record.businessType} ${record.payStatus}`}
    />
  );
}

export default OrderPage;
