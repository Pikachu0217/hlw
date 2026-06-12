import { Button, Divider, Space, Tag } from "antd-mobile";
import { useNavigate } from "react-router-dom";
import { SectionCard } from "../../../components/SectionCard";

export function DoctorDetailPage() {
  const navigate = useNavigate();

  return (
    <SectionCard title="医生详情" description="展示医生简介、擅长方向与接诊入口。">
      <Space direction="vertical" block>
        <div className="detail-title">李医生</div>
        <Tag color="success">主任医师</Tag>
        <div className="detail-copy">擅长儿童呼吸系统疾病、发热管理与过敏体质评估。</div>
        <Divider />
        <Button color="primary" block onClick={() => navigate("/appointment/confirm")}>
          预约挂号
        </Button>
        <Button block onClick={() => navigate("/consult/create")}>
          发起图文问诊
        </Button>
      </Space>
    </SectionCard>
  );
}
