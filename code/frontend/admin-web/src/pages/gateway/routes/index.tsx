import { Button, Descriptions, Form, Input, InputNumber, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createGatewayRoute, deleteGatewayRoute, fetchGatewayRouteDetail, fetchGatewayRoutes, updateGatewayRoute } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface GatewayRouteRecord {
  key: string;
  routeCode: string;
  uri: string;
  pathPredicate: string;
  sort: number;
  status: string;
  remark: string;
}

function GatewayRoutesPage() {
  const { records, loading, refresh } = useModuleRecords(fetchGatewayRoutes, '网关路由');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<GatewayRouteRecord | null>(null);
  const [detailRecord, setDetailRecord] = useState<GatewayRouteRecord | null>(null);

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ sort: 0, status: '0' });
    setOpen(true);
  };

  const handleOpenEdit = (record: GatewayRouteRecord) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setOpen(true);
  };

  const handleOpenDetail = async (record: GatewayRouteRecord) => {
    try {
      const detail = await fetchGatewayRouteDetail(record.key);
      setDetailRecord(detail);
      setDetailOpen(true);
    } catch {
      message.warning('网关路由详情加载失败，请稍后重试');
    }
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateGatewayRoute(editingRecord.key, values);
        message.success('网关路由更新成功');
      } else {
        await createGatewayRoute(values);
        message.success('网关路由创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning(editingRecord ? '网关路由更新失败，请检查接口或稍后重试' : '网关路由创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: GatewayRouteRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除路由"${record.routeCode}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteGatewayRoute(record.key);
          message.success('网关路由删除成功');
          refresh();
        } catch {
          message.warning('网关路由删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<GatewayRouteRecord>>(
    () => [
      { title: '路由编码', dataIndex: 'routeCode' },
      { title: '服务地址', dataIndex: 'uri' },
      { title: '路径断言', dataIndex: 'pathPredicate' },
      { title: '排序', dataIndex: 'sort' },
      { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '启用' : '禁用'}</Tag> },
      { title: '备注', dataIndex: 'remark' },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: GatewayRouteRecord) => (
          <Space size="small">
            <Button type="link" size="small" onClick={() => handleOpenDetail(record)}>
              详情
            </Button>
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
      <ModulePage<GatewayRouteRecord>
        eyebrow="网关管理"
        title="网关路由配置"
        description="维护管理端与业务服务的静态路由配置，当前版本只做配置管理。"
        metrics={[
          { label: '路由总数', value: String(records.length), hint: '来自后端网关配置接口' },
          { label: '启用路由', value: String(records.filter((record) => record.status === '0').length), hint: '按状态统计' },
          { label: '禁用路由', value: String(records.filter((record) => record.status !== '0').length), hint: '用于临时下线路由' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="网关路由列表"
        searchPlaceholder="搜索路由编码、服务地址或路径"
        getSearchText={(record) => `${record.routeCode} ${record.uri} ${record.pathPredicate} ${record.remark}`}
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑网关路由' : '新增网关路由'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="routeCode" label="路由编码" rules={[{ required: true, message: '请输入路由编码' }]}>
            <Input placeholder="例如：hospital-auth" />
          </Form.Item>
          <Form.Item name="uri" label="服务地址" rules={[{ required: true, message: '请输入服务地址' }]}>
            <Input placeholder="例如：lb://hospital-auth" />
          </Form.Item>
          <Form.Item name="pathPredicate" label="路径断言" rules={[{ required: true, message: '请输入路径断言' }]}>
            <Input placeholder="例如：/auth/**" />
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
      <Modal title="网关路由详情" open={detailOpen} onCancel={() => setDetailOpen(false)} footer={null} destroyOnClose>
        {detailRecord ? (
          <Descriptions column={1} size="small" bordered>
            <Descriptions.Item label="路由编码">{detailRecord.routeCode}</Descriptions.Item>
            <Descriptions.Item label="服务地址">{detailRecord.uri}</Descriptions.Item>
            <Descriptions.Item label="路径断言">{detailRecord.pathPredicate}</Descriptions.Item>
            <Descriptions.Item label="排序">{detailRecord.sort}</Descriptions.Item>
            <Descriptions.Item label="状态">{detailRecord.status}</Descriptions.Item>
            <Descriptions.Item label="备注">{detailRecord.remark || '-'}</Descriptions.Item>
          </Descriptions>
        ) : null}
      </Modal>
    </>
  );
}

export default GatewayRoutesPage;
