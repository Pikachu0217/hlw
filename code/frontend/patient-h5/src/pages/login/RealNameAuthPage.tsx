import { Button, Form, Input, Toast } from "antd-mobile";
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchPatientProfile, updatePatientProfile } from "../../app/api";
import { useSessionStore } from "../../store/sessionStore";

interface RealNameFormValues {
  patientName: string;
  idCard: string;
}

/** 身份证号正则（18 位，末位允许 X/x）。 */
const ID_CARD_REGEX = /^[1-9]\d{5}(?:18|19|20)\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/;

/** 盾牌认证图标（SVG）。 */
function ShieldIcon() {
  return (
    <svg className="login-medical-icon" viewBox="0 0 64 64" width="64" height="64" fill="none">
      <circle cx="32" cy="32" r="30" fill="rgba(28, 171, 143, 0.12)" stroke="#1cab8f" strokeWidth="2" />
      <path d="M32 14L46 20v9.5c0 8.5-5.8 16.5-14 19-8.2-2.5-14-10.5-14-19V20l14-6z" fill="rgba(28, 171, 143, 0.2)" stroke="#1cab8f" strokeWidth="1.5" />
      <path d="M28 33l3 3 6-7" stroke="#1cab8f" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

export function RealNameAuthPage() {
  const navigate = useNavigate();
  const phone = useSessionStore((state) => state.phone);
  const setPatientName = useSessionStore((state) => state.setPatientName);
  const setVerified = useSessionStore((state) => state.setVerified);
  const [form] = Form.useForm<RealNameFormValues>();
  const [submitting, setSubmitting] = useState(false);

  /** 跳过实名（演示用）。 */
  function handleSkip(): void {
    setVerified(true);
    navigate("/", { replace: true });
  }

  /** 提交实名认证。 */
  async function handleSubmit(values: RealNameFormValues): Promise<void> {
    setSubmitting(true);
    try {
      const profile = await updatePatientProfile({
        patientName: values.patientName,
        gender: "未知",
        age: 0,
        phone: phone || "未知",
        idCard: values.idCard
      });
      setPatientName(profile.patientName);
      setVerified(true);
      Toast.show("实名认证成功");
      navigate("/", { replace: true });
    } catch {
      Toast.show("实名认证失败，请稍后重试");
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="login-page">
      <div className="login-banner login-banner--compact">
        <ShieldIcon />
        <h1 className="login-banner-title">实名认证</h1>
        <p className="login-banner-desc">根据互联网诊疗规范，请先完成实名认证</p>
      </div>

      <div className="login-card">
        <div className="login-card-header">
          <h2 className="login-card-title">填写身份信息</h2>
          <p className="login-card-subtitle">确保信息与身份证一致</p>
        </div>

        <Form
          form={form}
          layout="horizontal"
          onFinish={handleSubmit}
          className="login-form"
        >
          <Form.Item
            name="patientName"
            rules={[
              { required: true, message: "请输入真实姓名" }
            ]}
          >
            <Input placeholder="请输入真实姓名" />
          </Form.Item>

          <Form.Item
            name="idCard"
            rules={[
              { required: true, message: "请输入身份证号" },
              { pattern: ID_CARD_REGEX, message: "身份证号格式不正确" }
            ]}
          >
            <Input placeholder="请输入身份证号" />
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
              提交认证
            </Button>
          </Form.Item>

          <Form.Item>
            <Button
              className="login-skip-btn"
              block
              fill="none"
              onClick={handleSkip}
            >
              跳过，稍后认证
            </Button>
          </Form.Item>
        </Form>
      </div>
    </div>
  );
}
