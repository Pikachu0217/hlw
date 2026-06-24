/**
 * 统一的状态颜色映射工具。
 * 将所有页面中重复定义的 switch-case 状态→颜色映射收口到这里。
 */

/** 根据状态值从映射表中查找对应的 Ant Design Tag 颜色。 */
export function statusColor(
  status: string,
  colorMap: Record<string, string>,
  fallback: string = 'default',
): string {
  // 精确匹配
  if (colorMap[status]) return colorMap[status];
  // 包含匹配（如 "已签到" 匹配 "签到"）
  for (const [key, color] of Object.entries(colorMap)) {
    if (status.includes(key)) return color;
  }
  return fallback;
}

/** 预约单状态 → 颜色 */
export function appointmentStatusColor(status: string): string {
  return statusColor(status, {
    '待支付': 'gold',
    '已支付': 'blue',
    '已签到': 'green',
    '已接单': 'cyan',
    '已完成': 'default',
    '已取消': 'red',
  }, 'processing');
}

/** 号源状态 → 颜色 */
export function numberSourceStatusColor(status: string): string {
  return statusColor(status, {
    AVAILABLE: 'green',
    LOCKED: 'gold',
    USED: 'blue',
    RELEASED: 'default',
  }, 'processing');
}

/** 问诊状态 → 颜色 */
export function consultStatusColor(status: string): string {
  return statusColor(status, {
    '待接单': 'gold',
    '咨询中': 'blue',
    '已延长': 'cyan',
  });
}

/** 订单状态 → 颜色 */
export function orderStatusColor(status: string): string {
  return statusColor(status, {
    '待支付': 'gold',
    '已支付': 'green',
    '已关闭': 'default',
    '已退款': 'red',
  }, 'processing');
}

/** 处方状态 → 颜色 */
export function prescriptionStatusColor(status: string): string {
  return statusColor(status, {
    '草稿': 'default',
    '待审方': 'gold',
    '待发药': 'green',
    '已驳回': 'red',
    '已发药': 'blue',
  }, 'processing');
}
