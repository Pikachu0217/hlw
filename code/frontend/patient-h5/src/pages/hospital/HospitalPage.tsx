import { List, Space, SpinLoading, Tag } from "antd-mobile";
import { useEffect, useState } from "react";
import { fetchHospitals, type HospitalItem } from "../../app/api";
import { SectionCard } from "../../components/SectionCard";

export function HospitalPage() {
  const [hospitals, setHospitals] = useState<HospitalItem[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    fetchHospitals()
      .then((records) => {
        if (!ignore) {
          setHospitals(records);
        }
      })
      .finally(() => {
        if (!ignore) {
          setLoading(false);
        }
      });

    return () => {
      ignore = true;
    };
  }, []);

  return (
    <SectionCard title="选择医院" description="支持多租户医院切换与服务能力展示。">
      {loading ? <SpinLoading /> : null}
      <List>
        {hospitals.map((hospital) => (
          <List.Item
            key={hospital.id}
            description={
              <Space>
                <Tag color="primary">{hospital.status === "0" ? "可选租户" : "暂停服务"}</Tag>
                <span>{hospital.tenantId}</span>
              </Space>
            }
          >
            {hospital.companyName}
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
