import { List, SpinLoading, Tag } from "antd-mobile";
import { useEffect, useState } from "react";
import { fetchPrescriptions, type PrescriptionItem } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

export function PrescriptionListPage() {
  const [prescriptions, setPrescriptions] = useState<PrescriptionItem[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    fetchPrescriptions()
      .then((records) => {
        if (!ignore) {
          setPrescriptions(records);
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
    <SectionCard title="我的处方" description="查看已开具处方、审核状态和配药进度。">
      {loading ? <SpinLoading /> : null}
      <List>
        {prescriptions.map((prescription) => (
          <List.Item key={prescription.id} extra={<Tag color={prescription.status.includes("待") ? "warning" : "success"}>{prescription.status}</Tag>}>
            {prescription.prescriptionNo} {prescription.doctorName}
          </List.Item>
        ))}
      </List>
    </SectionCard>
  );
}
