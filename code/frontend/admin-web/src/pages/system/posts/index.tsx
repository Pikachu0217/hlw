import { Form, Input, InputNumber, Modal, Select, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createPost, fetchPosts } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface PostRecord {
  key: string;
  postName: string;
  postCode: string;
  sort: number;
  status: string;
  remark: string;
}

const columns: ColumnsType<PostRecord> = [
  { title: '岗位名称', dataIndex: 'postName' },
  { title: '岗位编码', dataIndex: 'postCode' },
  { title: '排序', dataIndex: 'sort' },
  { title: '备注', dataIndex: 'remark' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function PostsPage() {
  const { records, loading, refresh } = useModuleRecords(fetchPosts, '岗位');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  const handleCreate = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await createPost(values);
      message.success('岗位创建成功');
      setOpen(false);
      form.resetFields();
      refresh();
    } catch {
      message.warning('岗位创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <ModulePage<PostRecord>
        eyebrow="系统管理"
        title="岗位管理"
        description="维护运营、药房、客服等岗位，并为用户绑定岗位提供基础数据。"
        metrics={[
          { label: '岗位数', value: String(records.length), hint: '来自后端岗位接口' },
          { label: '启用岗位', value: String(records.filter((record) => record.status === '启用').length), hint: '按状态实时统计' },
          { label: '编码覆盖', value: String(records.filter((record) => record.postCode).length), hint: '用于权限和人员编排' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="岗位列表"
        searchPlaceholder="搜索岗位名称、编码或备注"
        getSearchText={(record) => `${record.postName} ${record.postCode} ${record.remark}`}
        onCreate={() => setOpen(true)}
      />
      <Modal
        title="新增岗位"
        open={open}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form" initialValues={{ sort: 0, status: '启用' }}>
          <Form.Item name="postName" label="岗位名称" rules={[{ required: true, message: '请输入岗位名称' }]}>
            <Input placeholder="请输入岗位名称" />
          </Form.Item>
          <Form.Item name="postCode" label="岗位编码" rules={[{ required: true, message: '请输入岗位编码' }]}>
            <Input placeholder="例如：OPERATOR" />
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

export default PostsPage;
