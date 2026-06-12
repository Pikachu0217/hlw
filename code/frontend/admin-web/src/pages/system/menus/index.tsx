import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import { fetchMenus } from '@/api/modules';
import ModulePage from '@/components/ModulePage';
import { useModuleRecords } from '@/hooks/useModuleRecords';

export interface MenuRecord {
  key: string;
  menuName: string;
  menuType: string;
  permission: string;
  routePath: string;
  status: string;
}

const columns: ColumnsType<MenuRecord> = [
  { title: '菜单名称', dataIndex: 'menuName' },
  { title: '类型', dataIndex: 'menuType' },
  { title: '权限标识', dataIndex: 'permission' },
  { title: '路由路径', dataIndex: 'routePath' },
  { title: '状态', dataIndex: 'status', render: (value: string) => <Tag color="green">{value}</Tag> },
];

function MenusPage() {
  const { records, loading } = useModuleRecords(fetchMenus, '菜单');

  return (
    <ModulePage<MenuRecord>
      eyebrow="系统管理"
      title="菜单与权限标识"
      description="把路由、权限标识与按钮位关系先搭好。"
      metrics={[
        { label: '菜单节点', value: String(records.length), hint: '来自后端菜单接口' },
        { label: '启用菜单', value: String(records.filter((record) => record.status === '启用').length), hint: '按状态实时统计' },
        { label: '权限标识', value: String(records.filter((record) => record.permission).length), hint: '覆盖当前返回菜单' },
      ]}
      columns={columns}
      dataSource={records}
      loading={loading}
      tableTitle="菜单配置"
      searchPlaceholder="搜索菜单、权限标识或路由"
      getSearchText={(record) => `${record.menuName} ${record.permission} ${record.routePath}`}
    />
  );
}

export default MenusPage;
