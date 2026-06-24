import { Button, Form, Input, Picker, TextArea, Toast } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { createConsult, fetchPatientDoctorDetail, type PatientDoctor } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

interface ConsultFormValues {
  consultType?: string[];
  chiefComplaint?: string;
}

export function ConsultCreatePage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [form] = Form.useForm<ConsultFormValues>();
  const doctorId = searchParams.get("doctorId") ?? "";
  const [doctor, setDoctor] = useState<PatientDoctor | null>(null);

  useEffect(() => {
    let ignore = false;

    if (!doctorId) {
      setDoctor(null);
      return () => {
        ignore = true;
      };
    }

    fetchPatientDoctorDetail(doctorId).then((record) => {
      if (!ignore) {
        setDoctor(record);
      }
    });

    return () => {
      ignore = true;
    };
  }, [doctorId]);

  async function handleCreateConsult(): Promise<void> {
    const values = await form.validateFields();
    const consultType = values.consultType?.[0] ?? "IMAGE_TEXT";
    const chiefComplaint = values.chiefComplaint ?? "";

    try {
      const consult = await createConsult(consultType, chiefComplaint, doctor ?? undefined);
      navigate(`/consult/chat?consultId=${consult.id}&remainingSeconds=0`);
    } catch {
      Toast.show("问诊服务暂不可用，请稍后重试");
    }
  }

  return (
    <SectionCard title="发起问诊" description="填写病情描述后即可进入图文问诊房间。">
      {doctor ? <div className="detail-copy">接诊医生：{doctor.name} · 问诊费 {doctor.consultFee} 元</div> : null}
      <Form form={form} layout="horizontal" footer={<Button color="primary" block onClick={handleCreateConsult}>开始问诊</Button>}>
        <Form.Item label="问诊类型" name="consultType" rules={[{ required: true, message: "请选择问诊类型" }]}>
          <Picker columns={[[{ label: "图文问诊", value: "IMAGE_TEXT" }, { label: "复诊续方", value: "FOLLOW_UP" }]]}>
            {(items) => {
              const label = items?.[0]?.label;
              return <Input readOnly value={typeof label === "string" ? label : ""} placeholder="请选择问诊类型" />;
            }}
          </Picker>
        </Form.Item>
        <Form.Item label="主诉" name="chiefComplaint" rules={[{ required: true, message: "请输入主诉" }]}>
          <TextArea rows={4} placeholder="请输入主要症状和持续时间" />
        </Form.Item>
      </Form>
    </SectionCard>
  );
}
