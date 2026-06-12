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
            key={hospital.key}
            description={
              <Space>
                <Tag color="primary">{hospital.packageName}</Tag>
                <span>{hospital.status}</span>
              </Space>
            }
          >
            {hospital.tenantName}
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
