import { Button, Divider, Space, Tag } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { fetchPatientDoctorDetail, type PatientDoctor } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

/**
 * 医生详情页。
 * 展示医生简介与图文问诊入口，点击后进入预约确认流程。
 */
export function DoctorDetailPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const doctorId = searchParams.get("doctorId") ?? "";
  const [doctor, setDoctor] = useState<PatientDoctor | null>(null);

  useEffect(() => {
    let ignore = false;

    fetchPatientDoctorDetail(doctorId).then((record) => {
      if (!ignore) {
        setDoctor(record);
      }
    });

    return () => {
      ignore = true;
    };
  }, [doctorId]);

  return (
    <SectionCard title="医生详情" description="展示医生简介与图文问诊入口。">
      <Space direction="vertical" block>
        <div className="detail-title">{doctor?.name ?? "加载中"}</div>
        <Tag color="success">{doctor?.title ?? "医生"}</Tag>
        <div className="detail-copy">{doctor?.specialty ?? "正在读取医生擅长方向"}</div>
        <Divider />
        <Button color="primary" block onClick={() => navigate(`/appointment/confirm?doctorId=${doctorId}&source=consult`)}>
          图文问诊
        </Button>
      </Space>
    </SectionCard>
  );
}
