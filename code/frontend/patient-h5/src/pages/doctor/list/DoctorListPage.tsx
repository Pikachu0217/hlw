import { Card, List, Space, SpinLoading, Tag } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { fetchPatientDoctors, type PatientDoctor } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

export function DoctorListPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const deptId = searchParams.get("deptId") ?? undefined;
  const departmentName = searchParams.get("departmentName") ?? "";
  const [doctors, setDoctors] = useState<PatientDoctor[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    let ignore = false;
    setLoading(true);

    fetchPatientDoctors(deptId)
      .then((records) => {
        if (!ignore) {
          setDoctors(records);
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
  }, [deptId]);

  return (
    <SectionCard title={departmentName ? `${departmentName}医生` : "医生列表"} description="可继续查看医生详情并进入预约或问诊流程。">
      {loading ? <SpinLoading /> : null}
      <List>
        {doctors.map((doctor) => (
          <List.Item
            key={doctor.id}
            onClick={() => navigate(`/doctor/detail?doctorId=${doctor.id}`)}
            description={
              <Card>
                <Space direction="vertical" block>
                  <Space justify="between" block>
                    <strong>{doctor.title}</strong>
                    <Tag color={doctor.consultStatus === "ONLINE" ? "success" : "warning"}>{doctor.status}</Tag>
                  </Space>
                  <span>{doctor.department} · 问诊费 {doctor.consultFee} 元</span>
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
