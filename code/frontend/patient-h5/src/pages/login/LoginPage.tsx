import { Button, Form, Input, Toast } from "antd-mobile";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchPatientProfile, phoneLogin, sendPhoneCode } from "../../app/api";
import { useSessionStore } from "../../store/sessionStore";

interface LoginFormValues {
  phone: string;
  smsCode: string;
}

/** 医疗十字图标（SVG）。 */
function MedicalIcon() {
  return (
    <svg className="login-medical-icon" viewBox="0 0 64 64" width="64" height="64" fill="none">
      <circle cx="32" cy="32" r="30" fill="rgba(28, 171, 143, 0.12)" stroke="#1cab8f" strokeWidth="2" />
      <rect x="27" y="18" width="10" height="28" rx="2" fill="#1cab8f" />
      <rect x="18" y="27" width="28" height="10" rx="2" fill="#1cab8f" />
    </svg>
  );
}

export function LoginPage() {
  const navigate = useNavigate();
  const setToken = useSessionStore((state) => state.setToken);
  const setPatientName = useSessionStore((state) => state.setPatientName);
  const setPhone = useSessionStore((state) => state.setPhone);
  const setVerified = useSessionStore((state) => state.setVerified);
  const [form] = Form.useForm<LoginFormValues>();
  const [sending, setSending] = useState(false);
  const [countdown, setCountdown] = useState(0);
  const [submitting, setSubmitting] = useState(false);

  /** 获取验证码。 */
  async function handleSendCode(): Promise<void> {
    const phone = form.getFieldValue("phone");
    if (!/^1[3-9]\d{9}$/.test(phone)) {
      Toast.show("请输入正确的手机号");
      return;
    }
    setSending(true);
    try {
      await sendPhoneCode(phone);
      Toast.show("验证码已发送（固定 1234）");
      setCountdown(60);
      const timer = setInterval(() => {
        setCountdown((prev) => {
          if (prev <= 1) {
            clearInterval(timer);
            return 0;
          }
          return prev - 1;
        });
      }, 1000);
    } catch {
      Toast.show("验证码发送失败");
    } finally {
      setSending(false);
    }
  }

  /** 手机号登录。 */
  async function handleLogin(values: LoginFormValues): Promise<void> {
    setSubmitting(true);
    try {
      const result = await phoneLogin(values.phone, values.smsCode);
      setToken(result.token);
      setPhone(values.phone);
      setPatientName(result.realName);
      try {
        const profile = await fetchPatientProfile();
        if (profile.idCard) {
          setVerified(true);
          navigate("/", { replace: true });
        } else {
          setVerified(false);
          navigate("/real-name-auth", { replace: true });
        }
      } catch {
        navigate("/real-name-auth", { replace: true });
      }
    } catch {
      Toast.show("登录失败，请检查手机号和验证码");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-banner">
        <MedicalIcon />
        <h1 className="login-banner-title">明亮互联网医院</h1>
        <p className="login-banner-desc">在线问诊 · 处方购药</p>
      </div>

      <div className="login-card">
        <div className="login-card-header">
          <h2 className="login-card-title">手机号登录</h2>
          <p className="login-card-subtitle">请输入手机号获取验证码</p>
        </div>

        <Form
          form={form}
          layout="horizontal"
          onFinish={handleLogin}
          className="login-form"
        >
          <Form.Item
            name="phone"
            rules={[
              { required: true, message: "请输入手机号" },
              { pattern: /^1[3-9]\d{9}$/, message: "手机号格式不正确" }
            ]}
          >
            <div className="login-phone-row">
              <Input
                placeholder="请输入手机号"
                className="login-phone-input"
              />
              <Button
                className="login-code-btn"
                color="primary"
                fill="none"
                size="small"
                loading={sending}
                disabled={countdown > 0}
                onClick={handleSendCode}
              >
                {countdown > 0 ? `${countdown}s 后重发` : "获取验证码"}
              </Button>
            </div>
          </Form.Item>

          <Form.Item
            name="smsCode"
            rules={[
              { required: true, message: "请输入验证码" }
            ]}
          >
            <Input placeholder="请输入验证码" />
          </Form.Item>

          <Form.Item>
            <Button
              className="login-submit-btn"
              color="primary"
              block
              type="submit"
              loading={submitting}
              size="large"
            >
              登录
            </Button>
          </Form.Item>
        </Form>

        <p className="login-tip">提示：演示阶段验证码固定为 1234</p>
      </div>
    </div>
  );
}
