import { Button, Form, Input, List, Modal, Space, Tag, TextArea, Toast } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  createHealthRecord,
  fetchHealthRecords,
  fetchPatientProfile,
  fetchPatients,
  logout as requestLogout,
  updatePatientProfile,
  type HealthRecordItem,
  type PatientProfile,
  type UpdatePatientProfilePayload
} from "../../app/api";
import { useSessionStore } from "../../store/sessionStore";
import { SectionCard } from "../../components/SectionCard";

interface ProfileFormValues extends Omit<UpdatePatientProfilePayload, "age"> {
  age: number | string;
}

export function ProfilePage() {
  const navigate = useNavigate();
  const patientName = useSessionStore((state) => state.patientName);
  const setPatientName = useSessionStore((state) => state.setPatientName);
  const logout = useSessionStore((state) => state.logout);
  const [profile, setProfile] = useState<PatientProfile | null>(null);
  const [patients, setPatients] = useState<PatientProfile[]>([]);
  const [healthRecords, setHealthRecords] = useState<HealthRecordItem[]>([]);
  const [loggingOut, setLoggingOut] = useState(false);
  const [profileForm] = Form.useForm<ProfileFormValues>();
  const [healthForm] = Form.useForm<{ title?: string; summary?: string; allergies?: string; history?: string }>();

  useEffect(() => {
    loadProfileData()
      .catch(() => {
        console.warn("[patient] 患者服务未连接，使用本地患者名称");
      });
  }, []);

  async function loadProfileData(): Promise<void> {
    const currentProfile = await fetchPatientProfile();
    setProfile(currentProfile);
    setPatientName(currentProfile.patientName);
    profileForm.setFieldsValue({
      patientName: currentProfile.patientName,
      gender: currentProfile.gender,
      age: currentProfile.age ?? 0,
      phone: currentProfile.phone ?? "",
      riskLevel: currentProfile.riskLevel,
      idCard: currentProfile.idCard,
      birthday: currentProfile.birthday,
      address: currentProfile.address,
      lastVisit: currentProfile.lastVisit
    });

    const [patientRecords, healthRecordRecords] = await Promise.all([
      fetchPatients(),
      fetchHealthRecords(currentProfile.id)
    ]);
    setPatients(patientRecords);
    setHealthRecords(healthRecordRecords);
  }

  async function handleUpdateProfile(): Promise<void> {
    const values = await profileForm.validateFields();
    try {
      const nextProfile = await updatePatientProfile({
        ...values,
        age: Number(values.age)
      });
      setProfile(nextProfile);
      setPatientName(nextProfile.patientName);
      Toast.show("患者档案已更新");
      await loadProfileData();
    } catch {
      Toast.show("患者档案更新失败");
    }
  }

  async function handleCreateHealthRecord(): Promise<void> {
    if (!profile) {
      Toast.show("请先加载患者档案");
      return;
    }

    const values = await healthForm.validateFields();
    try {
      await createHealthRecord({
        patientId: profile.id,
        title: values.title ?? "",
        summary: values.summary ?? "",
        allergies: values.allergies,
        history: values.history
      });
      healthForm.resetFields();
      Toast.show("健康档案已新增");
      setHealthRecords(await fetchHealthRecords(profile.id));
    } catch {
      Toast.show("健康档案新增失败");
    }
  }

  function handleLogout(): void {
    Modal.confirm({
      title: "退出登录",
      content: "确认退出当前账号吗？",
      confirmText: "退出",
      cancelText: "取消",
      onConfirm: async () => {
        setLoggingOut(true);
        try {
          await requestLogout();
          Toast.show("已退出登录");
        } catch {
          Toast.show("已清除本地登录状态");
        } finally {
          logout();
          setLoggingOut(false);
          navigate("/login", { replace: true });
        }
      }
    });
  }

  return (
    <Space direction="vertical" block className="mobile-stack">
      <div className="hero-card">
        <Tag color="primary">患者档案</Tag>
        <div className="hero-title">{patientName}</div>
        <div className="hero-copy">
          {profile?.maskedPhone ?? "默认已注入本地登录令牌，用于访问患者服务接口。"}
        </div>
      </div>

      <SectionCard title="档案维护" description="对接当前患者档案更新接口。">
        <Form form={profileForm} layout="horizontal" footer={<Button color="primary" block onClick={handleUpdateProfile}>保存档案</Button>}>
          <Form.Item label="姓名" name="patientName" rules={[{ required: true, message: "请输入姓名" }]}>
            <Input placeholder="请输入姓名" />
          </Form.Item>
          <Form.Item label="性别" name="gender" rules={[{ required: true, message: "请输入性别" }]}>
            <Input placeholder="男/女" />
          </Form.Item>
          <Form.Item label="年龄" name="age" rules={[{ required: true, message: "请输入年龄" }]}>
            <Input type="number" placeholder="请输入年龄" />
          </Form.Item>
          <Form.Item label="电话" name="phone" rules={[{ required: true, message: "请输入电话" }]}>
            <Input placeholder="请输入联系电话" />
          </Form.Item>
          <Form.Item label="风险" name="riskLevel">
            <Input placeholder="LOW/MEDIUM/HIGH" />
          </Form.Item>
          <Form.Item label="地址" name="address">
            <Input placeholder="请输入联系地址" />
          </Form.Item>
        </Form>
      </SectionCard>

      <SectionCard title="就诊人管理" description="读取患者列表和当前患者详情。">
        <List>
          {patients.map((patient) => (
            <List.Item
              key={patient.id}
              description={`${patient.gender ?? "-"} · ${patient.maskedPhone ?? patient.phone ?? "-"}`}
              extra={<Tag color={patient.id === profile?.id ? "success" : "primary"}>{patient.riskLevel ?? "普通"}</Tag>}
            >
              {patient.patientName}
            </List.Item>
          ))}
        </List>
      </SectionCard>

      <SectionCard title="健康档案" description="新增和查看当前患者健康档案。">
        <List>
          {healthRecords.map((record) => (
            <List.Item
              key={record.id}
              description={record.summary}
              onClick={() => Modal.alert({ title: record.title, content: record.diagnosis || record.remark || record.summary })}
            >
              {record.title}
            </List.Item>
          ))}
        </List>
        <Form form={healthForm} layout="horizontal" className="profile-health-form" footer={<Button block color="primary" onClick={handleCreateHealthRecord}>新增健康档案</Button>}>
          <Form.Item label="标题" name="title" rules={[{ required: true, message: "请输入档案标题" }]}>
            <Input placeholder="如：慢病复诊记录" />
          </Form.Item>
          <Form.Item label="摘要" name="summary" rules={[{ required: true, message: "请输入档案摘要" }]}>
            <TextArea rows={2} placeholder="请输入档案摘要" />
          </Form.Item>
          <Form.Item label="过敏史" name="allergies">
            <Input placeholder="无或具体过敏史" />
          </Form.Item>
          <Form.Item label="既往史" name="history">
            <Input placeholder="请输入既往病史" />
          </Form.Item>
        </Form>
      </SectionCard>

      <Button color="primary" block>
        联系在线客服
      </Button>

      <div className="profile-logout-area">
        <Button block color="danger" fill="outline" loading={loggingOut} onClick={handleLogout}>
          退出登录
        </Button>
      </div>
    </Space>
  );
}
