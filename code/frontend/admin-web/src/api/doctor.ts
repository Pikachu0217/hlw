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
  schedule: string;
  patientCount: number;
}

// 查询医生列表，并适配表格 rowKey。
export async function fetchDoctors(): Promise<DoctorRecord[]> {
  console.info('[doctor] 查询后端医生列表');
  const response = await apiClient.get<ApiResult<BackendDoctor[]>>('/doctor/doctors');

  return response.data.data.map((doctor) => ({
    key: doctor.key ?? String(doctor.id),
    name: doctor.name,
    title: doctor.title,
    department: doctor.department,
    specialty: doctor.specialty,
    status: doctor.status,
    schedule: doctor.schedule,
    patientCount: doctor.patientCount,
  }));
}
