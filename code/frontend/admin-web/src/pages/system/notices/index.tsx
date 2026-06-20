import { Button, Form, Input, Modal, Select, Space, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useMemo, useState } from 'react';
import { createNotice, deleteNotice, fetchNotices, updateNotice } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface NoticeRecord {
  id: number;
  noticeTitle: string;
  noticeType: string;
  noticeContent?: string;
  status: string;
  remark?: string;
}

const noticeTypeMap: Record<string, string> = {
  '1': '通知',
  '2': '公告',
};

function NoticesPage() {
  const { records, loading, refresh } = useModuleRecords(fetchNotices, '通知公告');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [editingRecord, setEditingRecord] = useState<NoticeRecord | null>(null);

  const handleOpenCreate = () => {
    setEditingRecord(null);
    form.resetFields();
    form.setFieldsValue({ noticeType: '1', status: '0' });
    setOpen(true);
  };

  const handleOpenEdit = (record: NoticeRecord) => {
    setEditingRecord(record);
    form.setFieldsValue(record);
    setOpen(true);
  };

  const handleSubmit = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      if (editingRecord) {
        await updateNotice(editingRecord.id, values);
        message.success('通知公告更新成功');
      } else {
        await createNotice(values);
        message.success('通知公告创建成功');
      }
      setOpen(false);
      setEditingRecord(null);
      form.resetFields();
      refresh();
    } catch {
      message.warning(editingRecord ? '通知公告更新失败，请检查接口或稍后重试' : '通知公告创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = (record: NoticeRecord) => {
    Modal.confirm({
      title: '确认删除',
      content: `确定要删除通知公告"${record.noticeTitle}"吗？`,
      okText: '确认',
      okType: 'danger',
      cancelText: '取消',
      onOk: async () => {
        try {
          await deleteNotice(record.id);
          message.success('通知公告删除成功');
          refresh();
        } catch {
          message.warning('通知公告删除失败，请稍后重试');
        }
      },
    });
  };

  const columns = useMemo<ColumnsType<NoticeRecord>>(
    () => [
      { title: '标题', dataIndex: 'noticeTitle' },
      { title: '类型', dataIndex: 'noticeType', render: (value: string) => noticeTypeMap[value] ?? value },
      { title: '内容', dataIndex: 'noticeContent' },
      { title: '备注', dataIndex: 'remark' },
      {
        title: '状态',
        dataIndex: 'status',
        render: (value: string) => <Tag color={value === '0' ? 'green' : 'default'}>{value === '0' ? '正常' : '停用'}</Tag>,
      },
      {
        title: '操作',
        key: 'actions',
        render: (_: unknown, record: NoticeRecord) => (
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
      <ModulePage<NoticeRecord>
        eyebrow="系统管理"
        title="通知公告"
        description="维护管理端公告与运营通知，支持按类型和状态管理发布内容。"
        metrics={[
          { label: '公告数', value: String(records.length), hint: '来自后端通知公告接口' },
          { label: '启用公告', value: String(records.filter((record) => record.status === '0').length), hint: '按状态实时统计' },
          { label: '通知类型', value: String(new Set(records.map((record) => record.noticeType)).size), hint: '按公告类型聚合' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="通知公告列表"
        searchPlaceholder="搜索标题、类型或内容"
        getSearchText={(record) => `${record.noticeTitle} ${record.noticeType} ${record.noticeContent ?? ''} ${record.remark ?? ''}`}
        tableClassName="system-compact-table"
        onCreate={handleOpenCreate}
      />
      <Modal
        title={editingRecord ? '编辑通知公告' : '新增通知公告'}
        open={open}
        confirmLoading={submitting}
        onOk={handleSubmit}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form">
          <Form.Item name="noticeTitle" label="公告标题" rules={[{ required: true, message: '请输入公告标题' }]}>
            <Input placeholder="请输入公告标题" />
          </Form.Item>
          <Form.Item name="noticeType" label="公告类型" rules={[{ required: true, message: '请选择公告类型' }]}>
            <Select
              options={[
                { label: '通知', value: '1' },
                { label: '公告', value: '2' },
              ]}
            />
          </Form.Item>
          <Form.Item name="noticeContent" label="公告内容">
            <Input.TextArea rows={4} placeholder="请输入公告内容" />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={[
                { label: '正常', value: '0' },
                { label: '停用', value: '1' },
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

export default NoticesPage;
