import { Button, Form, Input, Picker, TextArea, Toast } from "antd-mobile";
import { useNavigate } from "react-router-dom";
import { createConsult } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

export function ConsultCreatePage() {
  const navigate = useNavigate();

  async function handleCreateConsult(): Promise<void> {
    try {
      const consult = await createConsult("孩子从昨晚开始发烧");
      navigate(`/consult/chat?consultId=${consult.id}`);
    } catch {
      Toast.show("问诊服务未连接，已进入本地演示聊天室");
      navigate("/consult/chat");
    }
  }

  return (
    <SectionCard title="发起问诊" description="填写病情描述后即可进入图文问诊房间。">
      <Form layout="horizontal" footer={<Button color="primary" block onClick={handleCreateConsult}>开始问诊</Button>}>
        <Form.Item label="问诊类型">
          <Picker columns={[[{ label: "图文问诊", value: "IMAGE_TEXT" }, { label: "复诊续方", value: "FOLLOW_UP" }]]}>
            {(items) => <Input readOnly value={items?.[0]?.label} placeholder="请选择问诊类型" />}
          </Picker>
        </Form.Item>
        <Form.Item label="主诉">
          <TextArea rows={4} placeholder="请输入主要症状和持续时间" />
        </Form.Item>
      </Form>
    </SectionCard>
  );
}
