import { App as AntApp, ConfigProvider } from 'antd';
import zhCN from 'antd/locale/zh_CN';
import { RouterProvider } from 'react-router-dom';
import { medicalConsoleTheme } from '@/app/theme';
import { appRouter } from '@/router/router';
import { AuthProvider } from '@/store/auth-store';

// 组装主题、鉴权与路由等全局能力。
function App() {
  return (
    <AntApp>
      <ConfigProvider locale={zhCN} theme={medicalConsoleTheme}>
        <AuthProvider>
          <RouterProvider router={appRouter} />
        </AuthProvider>
      </ConfigProvider>
    </AntApp>
  );
}

export default App;
