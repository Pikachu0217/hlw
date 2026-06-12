import { List, Space, Tag } from "antd-mobile";
import { SectionCard } from "../../components/SectionCard";

const hospitals = [
  { name: "华林云总医院", distance: "2.4km", tag: "三甲" },
  { name: "江南互联网医院", distance: "5.1km", tag: "儿科强项" }
];

export function HospitalPage() {
  return (
    <SectionCard title="选择医院" description="支持多租户医院切换与服务能力展示。">
      <List>
        {hospitals.map((hospital) => (
          <List.Item
            key={hospital.name}
            description={
              <Space>
                <Tag color="primary">{hospital.tag}</Tag>
                <span>{hospital.distance}</span>
              </Space>
            }
          >
            {hospital.name}
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
