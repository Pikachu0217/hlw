import { Form, Input, Modal, Select, Tag, message } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { useState } from 'react';
import { createRole, fetchRoles } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface RoleRecord {
  key: string;
  roleName: string;
  dataScope: string;
  memberCount: number;
  updatedAt: string;
  status: string;
}

const columns: ColumnsType<RoleRecord> = [
  { title: '角色名称', dataIndex: 'roleName' },
  { title: '数据范围', dataIndex: 'dataScope' },
  { title: '成员数', dataIndex: 'memberCount' },
  { title: '更新时间', dataIndex: 'updatedAt' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function RolesPage() {
  const { records, loading, refresh } = useModuleRecords(fetchRoles, '角色');
  const [form] = Form.useForm();
  const [open, setOpen] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const memberCount = records.reduce((sum, record) => sum + record.memberCount, 0);

  const handleCreate = async () => {
    const values = await form.validateFields();
    setSubmitting(true);
    try {
      await createRole(values);
      message.success('角色创建成功');
      setOpen(false);
      form.resetFields();
      refresh();
    } catch {
      message.warning('角色创建失败，请检查接口或稍后重试');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <>
      <ModulePage<RoleRecord>
        eyebrow="系统管理"
        title="角色与数据范围"
        description="先把角色列表、数据范围和启停状态搭清楚。"
        metrics={[
          { label: '角色数', value: String(records.length), hint: '来自后端角色接口' },
          { label: '成员数', value: String(memberCount), hint: '汇总当前角色成员' },
          { label: '启用角色', value: String(records.filter((record) => record.status === '启用').length), hint: '按状态实时统计' },
        ]}
        columns={columns}
        dataSource={records}
        loading={loading}
        tableTitle="角色列表"
        searchPlaceholder="搜索角色名称或数据范围"
        getSearchText={(record) => `${record.roleName} ${record.dataScope} ${record.status}`}
        onCreate={() => setOpen(true)}
      />
      <Modal
        title="新增角色"
        open={open}
        confirmLoading={submitting}
        onOk={handleCreate}
        onCancel={() => setOpen(false)}
        destroyOnClose
      >
        <Form form={form} layout="vertical" className="module-form" initialValues={{ dataScope: '本租户数据', status: '启用' }}>
          <Form.Item name="roleName" label="角色名称" rules={[{ required: true, message: '请输入角色名称' }]}>
            <Input placeholder="请输入角色名称" />
          </Form.Item>
          <Form.Item name="roleCode" label="角色编码" rules={[{ required: true, message: '请输入角色编码' }]}>
            <Input placeholder="例如：OPERATOR" />
          </Form.Item>
          <Form.Item name="dataScope" label="数据范围">
            <Select
              options={[
                { label: '本租户数据', value: '本租户数据' },
                { label: '本科室数据', value: '本科室数据' },
                { label: '全部数据', value: '全部数据' },
              ]}
            />
          </Form.Item>
          <Form.Item name="status" label="状态">
            <Select
              options={[
                { label: '启用', value: '启用' },
                { label: '停用', value: '停用' },
              ]}
            />
          </Form.Item>
        </Form>
      </Modal>
    </>
  );
}

export default RolesPage;
