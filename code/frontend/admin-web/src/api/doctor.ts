import { apiClient } from '@/api/client';
import type { DoctorRecord } from '@/pages/doctor/components/DoctorList';

interface ApiResult<T> {
  code: number;
  message: string;
  data: T;
}

interface BackendDoctor {
  id: number;
  key?: string;
  name: string;
  title: string;
  department: string;
  specialty: string;
  status: string;
  consultStatus: string;
  schedule: string;
  patientCount: number;
  consultFee: string;
}

// 查询医生列表，并适配表格 rowKey。
export async function fetchDoctors(): Promise<DoctorRecord[]> {
  console.info('[doctor] 查询后端医生列表');
  const response = await apiClient.get<ApiResult<BackendDoctor[]>>('/doctor/doctors');

  return response.data.data.map((doctor) => ({
    id: doctor.id,
    key: doctor.key ?? String(doctor.id),
    name: doctor.name,
    title: doctor.title,
    department: doctor.department,
    specialty: doctor.specialty,
    status: doctor.status,
    consultStatus: doctor.consultStatus,
    schedule: doctor.schedule,
    patientCount: doctor.patientCount,
    consultFee: doctor.consultFee,
  }));
}
