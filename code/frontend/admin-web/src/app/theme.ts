import type { ThemeConfig } from 'antd';

// 定义明亮医疗控制台的主题令牌。
export const medicalConsoleTheme: ThemeConfig = {
  token: {
    colorPrimary: '#147d8f',
    colorInfo: '#147d8f',
    colorSuccess: '#36a685',
    colorWarning: '#f59e0b',
    colorError: '#d95f5f',
    colorText: '#123147',
    colorTextSecondary: '#547087',
    colorBgLayout: '#f3f8fb',
    colorBgContainer: 'rgba(255, 255, 255, 0.88)',
    borderRadius: 18,
    fontFamily:
      '"PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", "Noto Sans SC", sans-serif',
    boxShadowSecondary: '0 18px 60px rgba(14, 42, 68, 0.08)',
  },
  components: {
    Layout: {
      bodyBg: '#f3f8fb',
      siderBg: 'rgba(8, 39, 62, 0.96)',
      headerBg: 'transparent',
      triggerBg: '#10324d',
    },
    Menu: {
      darkItemBg: 'transparent',
      darkItemColor: 'rgba(226, 241, 248, 0.76)',
      darkItemHoverBg: 'rgba(87, 174, 198, 0.16)',
      darkItemSelectedColor: '#ffffff',
      darkSubMenuItemBg: 'transparent',
    },
    Card: {
      bodyPadding: 22,
    },
    Button: {
      controlHeight: 42,
      borderRadius: 14,
    },
    Input: {
      controlHeight: 42,
      borderRadius: 14,
    },
    Table: {
      borderColor: 'rgba(18, 49, 71, 0.08)',
      headerBg: 'rgba(20, 125, 143, 0.08)',
      rowHoverBg: 'rgba(20, 125, 143, 0.04)',
    },
  },
};
