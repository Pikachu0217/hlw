import { Button, Divider, Space, Tag } from "antd-mobile";
import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { fetchPatientDoctorDetail, type PatientDoctor } from "../../../app/api";
import { SectionCard } from "../../../components/SectionCard";

export function DoctorDetailPage() {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const doctorId = Number(searchParams.get("doctorId") ?? 1);
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
    <SectionCard title="医生详情" description="展示医生简介、擅长方向与接诊入口。">
      <Space direction="vertical" block>
        <div className="detail-title">{doctor?.name ?? "加载中"}</div>
        <Tag color="success">{doctor?.title ?? "医生"}</Tag>
        <div className="detail-copy">{doctor?.specialty ?? "正在读取医生擅长方向"}</div>
        <Divider />
        <Button color="primary" block onClick={() => navigate(`/appointment/confirm?doctorId=${doctorId}`)}>
          预约挂号
        </Button>
        <Button block onClick={() => navigate("/consult/create")}>
          发起图文问诊
        </Button>
      </Space>
    </SectionCard>
  );
}
