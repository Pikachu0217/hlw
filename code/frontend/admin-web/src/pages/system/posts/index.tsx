import { Button, Form, Input, InputNumber, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createPost, deletePost, fetchPosts, updatePost } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface PostRecord {
  key: string;
  postName: string;
  postCode: string;
  orderNum?: number;
  status: number;
  remark?: string;
}

function PostsPage() {
  const { records, loading, refresh } = useModuleRecords(fetchPosts, '岗位');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<PostRecord | null>(null);

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ orderNum: 0, status: 0 });
    setOpen(true);
  };

  const handleOpenEdit = (record: PostRecord) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updatePost(editingRecord.key, values);
        message.success('岗位更新成功');
      } else {
        await createPost(values);
        message.success('岗位创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning(editingRecord ? '岗位更新失败，请检查接口或稍后重试' : '岗位创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: PostRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除岗位"${record.postName}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deletePost(record.key);
          message.success('岗位删除成功');
          refresh();
        } catch {
          message.warning('岗位删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<PostRecord>>(
    () => [
      { title: '岗位名称', dataIndex: 'postName' },
      { title: '岗位编码', dataIndex: 'postCode' },
      { title: '排序', dataIndex: 'orderNum' },
      { title: '备注', dataIndex: 'remark' },
      {
        title: '状态',
        dataIndex: 'status',
        render: (value: number) => <Tag color={value === 0 ? 'green' : 'default'}>{value === 0 ? '启用' : '禁用'}</Tag>,
      },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: PostRecord) => (
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
      <ModulePage<PostRecord>
        eyebrow="系统管理"
        title="岗位管理"
        description="维护运营、药房、客服等岗位，并为用户绑定岗位提供基础数据。"
        metrics={[
          { label: '岗位数', value: String(records.length), hint: '来自后端岗位接口' },
          { label: '启用岗位', value: String(records.filter((record) => record.status === 0).length), hint: '按状态实时统计' },
          { label: '编码覆盖', value: String(records.filter((record) => record.postCode).length), hint: '用于权限和人员编排' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="岗位列表"
        searchPlaceholder="搜索岗位名称、编码或备注"
        getSearchText={(record) => `${record.postName} ${record.postCode} ${record.remark ?? ''}`}
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑岗位' : '新增岗位'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="postName" label="岗位名称" rules={[{ required: true, message: '请输入岗位名称' }]}>
            <Input placeholder="请输入岗位名称" />
          </Form.Item>
          <Form.Item name="postCode" label="岗位编码" rules={[{ required: true, message: '请输入岗位编码' }]}>
            <Input placeholder="例如：OPERATOR" />
          </Form.Item>
          <Form.Item name="orderNum" label="排序">
            <InputNumber min={0} className="module-form__number" />
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

export default PostsPage;
