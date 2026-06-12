import { Card, List, Space, Tag } from "antd-mobile";
import { useNavigate } from "react-router-dom";
import { SectionCard } from "../../../components/SectionCard";

const doctors = [
  { name: "李医生", title: "主任医师", fee: "50.00", status: "在线" },
  { name: "周医生", title: "副主任医师", fee: "30.00", status: "忙碌" }
];

export function DoctorListPage() {
  const navigate = useNavigate();

  return (
    <SectionCard title="医生列表" description="可继续查看医生详情并进入预约或问诊流程。">
      <List>
        {doctors.map((doctor) => (
          <List.Item
            key={doctor.name}
            onClick={() => navigate("/doctor/detail")}
            description={
              <Card>
                <Space direction="vertical" block>
                  <Space justify="between" block>
                    <strong>{doctor.title}</strong>
                    <Tag color={doctor.status === "在线" ? "success" : "warning"}>{doctor.status}</Tag>
                  </Space>
                  <span>问诊费 {doctor.fee} 元</span>
                </Space>
              </Card>
            }
          >
            {doctor.name}
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
