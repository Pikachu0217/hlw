import { LockOutlined, ShopOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Card, Checkbox, Form, Input, Select, Space, Tag, Typography, message } from 'antd';
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { loginAdmin } from '@/api/auth';
import { fetchTenantOptions, type TenantOptionRecord } from '@/api/modules';
import { useAuthStore } from '@/store/auth-store';

interface LoginFormValues {
  tenantId: number;
  username: string;
  password: string;
  remember: boolean;
}

function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuthStore();
  const [form] = Form.useForm<LoginFormValues>();
  const [tenants, setTenants] = useState<TenantOptionRecord[]>([]);
  const [tenantLoading, setTenantLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    let ignore = false;

    async function loadTenants(): Promise<void> {
      setTenantLoading(true);
      try {
        const tenantRecords = await fetchTenantOptions();
        if (ignore) {
          return;
        }
        setTenants(tenantRecords);
        if (tenantRecords.length > 0) {
          form.setFieldValue('tenantId', Number(tenantRecords[0].tenantId));
        }
      } catch {
        if (!ignore) {
          message.warning('租户列表加载失败，请确认后端系统服务已启动');
        }
      } finally {
        if (!ignore) {
          setTenantLoading(false);
        }
      }
    }

    loadTenants();
    return () => {
      ignore = true;
    };
  }, [form]);

  async function handleFinish(values: LoginFormValues): Promise<void> {
    setSubmitting(true);
    try {
      const snapshot = await loginAdmin(values);
      login(snapshot);

      const redirectPath = (location.state as { from?: string } | null)?.from ?? '/dashboard';
      navigate(redirectPath, { replace: true });
    } catch {
      message.error('登录失败，请确认租户、账号和密码是否正确');
    } finally {
      setSubmitting(false);
    }
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
      </section>
      <Card className="login-card" bordered={false}>
        <Space direction="vertical" size={20} className="login-card__head">
          <div>
            <Typography.Title level={3} className="login-card__title">
              登录管理台
            </Typography.Title>
            <Typography.Text className="login-card__subtitle">租户、账号和密码会提交到后端认证服务并写入登录令牌。</Typography.Text>
          </div>
        </Space>
        <Form<LoginFormValues>
          form={form}
          layout="vertical"
          initialValues={{ username: '', password: '', remember: true }}
          onFinish={handleFinish}
        >
          <Form.Item
            label={
              <span className="login-field-label">
                <ShopOutlined />
                租户
              </span>
            }
            name="tenantId"
            rules={[{ required: true, message: '请选择租户' }]}
          >
            <Select
              loading={tenantLoading}
              placeholder="请选择登录租户"
              options={tenants.map((tenant) => ({
                label: tenant.companyName,
                value: Number(tenant.tenantId),
              }))}
              showSearch
              optionFilterProp="label"
            />
          </Form.Item>
          <Form.Item label="账号" name="username" rules={[{ required: true, message: '请输入账号' }]}>
            <Input prefix={<UserOutlined />} placeholder="请输入管理账号" />
          </Form.Item>
          <Form.Item label="密码" name="password" rules={[{ required: true, message: '请输入密码' }]}>
            <Input.Password prefix={<LockOutlined />} placeholder="请输入登录密码" />
          </Form.Item>
          <Form.Item name="remember" valuePropName="checked">
            <Checkbox>记住本次演示账号</Checkbox>
          </Form.Item>
          <Button type="primary" htmlType="submit" block loading={submitting}>
            进入控制台
          </Button>
        </Form>
      </Card>
    </div>
  );
}

export default LoginPage;
