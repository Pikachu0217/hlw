import { LockOutlined, SafetyCertificateOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Card, Checkbox, Form, Input, Space, Tag, Typography } from 'antd';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuthStore } from '@/store/auth-store';

interface LoginFormValues {
  username: string;
  password: string;
  remember: boolean;
}

// 渲染管理端登录页，并写入演示 satoken。
function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuthStore();

  // 处理演示登录逻辑，为后续真实接口接入预留入口。
  function handleFinish(values: LoginFormValues): void {
    login({
      token: `satoken-demo-${values.username}`,
      displayName: values.username || '医疗运营专员',
      roleName: '系统管理员',
    });

    const redirectPath = (location.state as { from?: string } | null)?.from ?? '/dashboard';
    navigate(redirectPath, { replace: true });
  }

  return (
    <div className="login-page">
      <section className="login-hero">
        <Tag color="cyan" bordered={false} className="login-hero__tag">
          HLW Admin
        </Tag>
        <Typography.Title className="login-hero__title">明亮医疗控制台</Typography.Title>
        <Typography.Paragraph className="login-hero__description">
          面向租户、系统、医生、患者、咨询、预约、处方、药品与订单的统一管理入口。
        </Typography.Paragraph>
        <div className="login-hero__metrics">
          <article>
            <strong>12</strong>
            <span>基础页面目录</span>
          </article>
          <article>
            <strong>satoken</strong>
            <span>请求自动携带</span>
          </article>
          <article>
            <strong>React 18</strong>
            <span>Vite + Ant Design</span>
          </article>
        </div>
      </section>
      <Card className="login-card" bordered={false}>
        <Space direction="vertical" size={20} className="login-card__head">
          <div>
            <Typography.Title level={3} className="login-card__title">
              登录管理台
            </Typography.Title>
            <Typography.Text className="login-card__subtitle">
              演示模式下提交后会直接写入本地 satoken。
            </Typography.Text>
          </div>
          <Tag color="blue" icon={<SafetyCertificateOutlined />}>
            已预留路由守卫与 401 自动失效处理
          </Tag>
        </Space>
        <Form<LoginFormValues>
          layout="vertical"
          initialValues={{ username: '运营主任', password: '123456', remember: true }}
          onFinish={handleFinish}
        >
          <Form.Item label="账号" name="username" rules={[{ required: true, message: '请输入账号' }]}>
            <Input prefix={<UserOutlined />} placeholder="请输入管理账号" />
          </Form.Item>
          <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="请输入登录密码" />
          </Form.Item>
          <Form.Item name="remember" valuePropName="checked">
            <Checkbox>记住本次演示账号</Checkbox>
          </Form.Item>
          <Button type="primary" htmlType="submit" block>
            进入控制台
          </Button>
        </Form>
      </Card>
    </div>
  );
}

export default LoginPage;
