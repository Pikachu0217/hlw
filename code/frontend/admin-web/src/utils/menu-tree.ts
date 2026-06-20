import type { DataNode } from 'antd/es/tree';

/** 顶级菜单父编号。 */
export const ROOT_MENU_PARENT_ID = 0;

/** 可组装为菜单树的基础菜单记录。 */
export interface MenuTreeSource {
  /** 菜单编号。 */
  id: number;
  /** 父级菜单编号。 */
  parentId?: number | null;
  /** 菜单名称。 */
  menuName: string;
  /** 权限标识。 */
  perms?: string;
  /** 排序号。 */
  orderNum?: number;
  /** 子菜单列表。 */
  children?: MenuTreeSource[];
}

/** 带子节点的菜单树记录。 */
export type MenuTreeRecord<T extends MenuTreeSource> = Omit<T, 'children'> & {
  /** 子菜单节点。 */
  children?: Array<MenuTreeRecord<T>>;
};

/** 菜单树选择节点。 */
export interface MenuTreeSelectNode {
  /** 节点标题。 */
  title: string;
  /** 节点值。 */
  value: number;
  /** 节点键。 */
  key: number;
  /** 是否禁用。 */
  disabled?: boolean;
  /** 子菜单节点。 */
  children?: MenuTreeSelectNode[];
}

/**
 * 将菜单列表按 parentId 组装为树。
 *
 * @param records 菜单列表
 * @return 菜单树
 */
export function buildMenuTree<T extends MenuTreeSource>(records: T[]): Array<MenuTreeRecord<T>> {
  const nodeMap = new Map<number, MenuTreeRecord<T>>();
  const roots: Array<MenuTreeRecord<T>> = [];
  const sortedRecords = flattenMenuRecords(records).sort(compareMenuRecord);

  sortedRecords.forEach((record) => {
    const { children: ignoredChildren, ...nodeRecord } = record;
    nodeMap.set(record.id, { ...nodeRecord } as MenuTreeRecord<T>);
  });

  sortedRecords.forEach((record) => {
    const node = nodeMap.get(record.id);
    if (!node) {
      return;
    }
    const parentId = normalizeParentId(record.parentId);
    const parent = nodeMap.get(parentId);
    if (parent && parent.id !== node.id) {
      parent.children = [...(parent.children ?? []), node];
      return;
    }
    roots.push(node);
  });

  return roots;
}

/**
 * 将菜单树或菜单列表展开为平铺列表。
 *
 * @param records 菜单记录列表
 * @return 平铺菜单记录列表
 */
export function flattenMenuTree<T extends MenuTreeSource>(records: T[]): T[] {
  return flattenMenuRecords(records);
}

/**
 * 将后端树形菜单或平铺菜单统一展开为平铺列表。
 *
 * @param records 菜单记录列表
 * @return 平铺菜单记录列表
 */
function flattenMenuRecords<T extends MenuTreeSource>(records: T[]): T[] {
  const flattened: T[] = [];
  records.forEach((record) => {
    flattened.push(record);
    if (record.children?.length) {
      flattened.push(...flattenMenuRecords(record.children as T[]));
    }
  });
  return flattened;
}

/**
 * 按关键字过滤菜单树，保留命中节点的父级链路。
 *
 * @param treeData 菜单树
 * @param keyword 搜索关键字
 * @param getSearchText 搜索文本提取函数
 * @return 过滤后的菜单树
 */
export function filterMenuTree<T extends MenuTreeSource>(
  treeData: Array<MenuTreeRecord<T>>,
  keyword: string,
  getSearchText: (record: MenuTreeRecord<T>) => string,
): Array<MenuTreeRecord<T>> {
  const normalizedKeyword = keyword.trim().toLowerCase();
  if (!normalizedKeyword) {
    return treeData;
  }

  return treeData
    .map((node) => filterMenuTreeNode(node, normalizedKeyword, getSearchText))
    .filter((node): node is MenuTreeRecord<T> => Boolean(node));
}

/**
 * 构造菜单绑定使用的勾选树。
 *
 * @param records 菜单列表
 * @return 勾选树节点
 */
export function buildMenuCheckTreeData<T extends MenuTreeSource>(records: T[]): DataNode[] {
  return buildMenuTree(records).map(toMenuCheckTreeNode);
}

/**
 * 构造角色菜单绑定使用的勾选树。
 *
 * @param records 菜单列表
 * @return 勾选树节点
 */
export function buildRoleMenuTreeData<T extends MenuTreeSource>(records: T[]): DataNode[] {
  return buildMenuCheckTreeData(records);
}

/**
 * 构造父级菜单选择树。
 *
 * @param records 菜单列表
 * @param disabledRootId 禁用的当前菜单根编号
 * @return 父级菜单选择树
 */
export function buildParentMenuTreeData<T extends MenuTreeSource>(
  records: T[],
  disabledRootId?: number,
): MenuTreeSelectNode[] {
  const disabledIds = disabledRootId ? collectDescendantMenuIds(records, disabledRootId) : new Set<number>();
  if (disabledRootId) {
    disabledIds.add(disabledRootId);
  }
  return [
    {
      title: '顶级菜单',
      value: ROOT_MENU_PARENT_ID,
      key: ROOT_MENU_PARENT_ID,
      children: buildMenuTree(records).map((node) => toParentMenuTreeNode(node, disabledIds)),
    },
  ];
}

/**
 * 比较菜单记录排序。
 *
 * @param current 当前菜单
 * @param next 下一个菜单
 * @return 排序结果
 */
function compareMenuRecord<T extends MenuTreeSource>(current: T, next: T): number {
  return (current.orderNum ?? ROOT_MENU_PARENT_ID) - (next.orderNum ?? ROOT_MENU_PARENT_ID)
    || current.id - next.id;
}

/**
 * 标准化父级菜单编号。
 *
 * @param parentId 原始父级菜单编号
 * @return 标准父级菜单编号
 */
function normalizeParentId(parentId?: number | null): number {
  return parentId ?? ROOT_MENU_PARENT_ID;
}

/**
 * 过滤单个菜单树节点。
 *
 * @param node 菜单树节点
 * @param keyword 搜索关键字
 * @param getSearchText 搜索文本提取函数
 * @return 命中的菜单树节点
 */
function filterMenuTreeNode<T extends MenuTreeSource>(
  node: MenuTreeRecord<T>,
  keyword: string,
  getSearchText: (record: MenuTreeRecord<T>) => string,
): MenuTreeRecord<T> | null {
  const children = (node.children ?? [])
    .map((child) => filterMenuTreeNode(child, keyword, getSearchText))
    .filter((child): child is MenuTreeRecord<T> => Boolean(child));
  const matched = getSearchText(node).toLowerCase().includes(keyword);
  if (!matched && children.length === 0) {
    return null;
  }
  return {
    ...node,
    ...(children.length > 0 ? { children } : {}),
  };
}

/**
 * 转换为菜单绑定树节点。
 *
 * @param node 菜单树节点
 * @return 菜单绑定树节点
 */
function toMenuCheckTreeNode<T extends MenuTreeSource>(node: MenuTreeRecord<T>): DataNode {
  return {
    title: node.perms ? `${node.menuName}（${node.perms}）` : node.menuName,
    key: node.id,
    children: node.children?.map(toMenuCheckTreeNode),
  };
}

/**
 * 转换为父级菜单选择树节点。
 *
 * @param node 菜单树节点
 * @param disabledIds 禁用菜单编号集合
 * @return 父级菜单选择树节点
 */
function toParentMenuTreeNode<T extends MenuTreeSource>(
  node: MenuTreeRecord<T>,
  disabledIds: Set<number>,
): MenuTreeSelectNode {
  return {
    title: node.menuName,
    value: node.id,
    key: node.id,
    disabled: disabledIds.has(node.id),
    children: node.children?.map((child) => toParentMenuTreeNode(child, disabledIds)),
  };
}

/**
 * 收集指定菜单的全部子孙菜单编号。
 *
 * @param records 菜单列表
 * @param rootId 根菜单编号
 * @return 子孙菜单编号集合
 */
function collectDescendantMenuIds<T extends MenuTreeSource>(records: T[], rootId: number): Set<number> {
  const childrenMap = new Map<number, number[]>();
  records.forEach((record) => {
    const parentId = normalizeParentId(record.parentId);
    childrenMap.set(parentId, [...(childrenMap.get(parentId) ?? []), record.id]);
  });

  const descendants = new Set<number>();
  const stack = [...(childrenMap.get(rootId) ?? [])];
  while (stack.length > 0) {
    const id = stack.pop();
    if (id == null || descendants.has(id)) {
      continue;
    }
    descendants.add(id);
    stack.push(...(childrenMap.get(id) ?? []));
  }
  return descendants;
}
