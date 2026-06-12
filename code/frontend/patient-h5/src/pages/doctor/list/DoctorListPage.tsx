import { Card, List, Space, Tag } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { fetchPatientDoctors, type PatientDoctor } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

const fallbackDoctors: PatientDoctor[] = [
  { id: 1, name: "陈知衡", title: "主任医师", department: "心内科", specialty: "冠脉慢病管理", consultFee: "50.00", status: "接诊中", consultStatus: "ONLINE" },
  { id: 2, name: "顾清和", title: "副主任医师", department: "内分泌科", specialty: "糖尿病营养干预", consultFee: "30.00", status: "候诊", consultStatus: "BUSY" }
];

export function DoctorListPage() {
  const navigate = useNavigate();
  const [doctors, setDoctors] = useState<PatientDoctor[]>(fallbackDoctors);

  useEffect(() => {
    let ignore = false;

    fetchPatientDoctors()
      .then((records) => {
        if (!ignore) {
          setDoctors(records);
        }
      })
      .catch(() => {
        console.warn("[patient] 医生服务未连接，使用本地兜底数据");
      });

    return () => {
      ignore = true;
    };
  }, []);

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
