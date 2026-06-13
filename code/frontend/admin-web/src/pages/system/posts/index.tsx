import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchPosts } from '@/api/modules';
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
  const { records, loading } = useModuleRecords(fetchPosts, '岗位');

  return (
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
    />
  );
}

export default PostsPage;
