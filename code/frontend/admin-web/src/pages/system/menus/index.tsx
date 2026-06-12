import { Tag } from 'antd';
import type { ColumnsType } from 'antd/es/table';
import ModulePage from '@/components/ModulePage';

interface MenuRecord {
  key: string;
  menuName: string;
  menuType: string;
  permission: string;
  routePath: string;
  status: string;
}

const dataSource: MenuRecord[] = [
  { key: '1', menuName: '工作台', menuType: '菜单', permission: 'dashboard:view', routePath: '/dashboard', status: '启用' },
  { key: '2', menuName: '医生管理', menuType: '菜单', permission: 'doctor:list', routePath: '/doctor', status: '启用' },
  { key: '3', menuName: '新增医生', menuType: '按钮', permission: 'doctor:create', routePath: 'N/A', status: '预留' },
];

const columns: ColumnsType<MenuRecord> = [
  { title: '菜单名称', dataIndex: 'menuName' },
  { title: '类型', dataIndex: 'menuType' },
  { title: '权限标识', dataIndex: 'permission' },
  { title: '路由路径', dataIndex: 'routePath' },
  {
    title: '状态',
    dataIndex: 'status',
    render: (value: string) => <Tag color={value === '启用' ? 'green' : 'blue'}>{value}</Tag>,
  },
];

// 渲染菜单管理基础页。
function MenusPage() {
  return (
    <ModulePage<MenuRecord>
      eyebrow="系统管理"
      title="菜单与权限标识"
      description="把路由、权限标识与按钮位关系先搭好，后续对接后端菜单树时只需替换数据源。"
      badgeText="与路由结构联动"
      metrics={[
        { label: '菜单节点', value: '18', hint: '含分组与业务菜单' },
        { label: '按钮权限', value: '26', hint: '覆盖新增、编辑、导出' },
        { label: '待补权限', value: '5', hint: '主要集中在订单与药品模块' },
      ]}
      columns={columns}
      dataSource={dataSource}
      tableTitle="菜单配置"
      searchPlaceholder="搜索菜单、权限标识或路由"
      getSearchText={(record) => `${record.menuName} ${record.permission} ${record.routePath}`}
    />
  );
}

export default MenusPage;
